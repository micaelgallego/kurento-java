/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.kurento.jsonrpc.client;

import static org.kurento.jsonrpc.JsonUtils.fromJson;
import static org.kurento.jsonrpc.JsonUtils.fromJsonRequest;
import static org.kurento.jsonrpc.JsonUtils.fromJsonResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.kurento.commons.exception.KurentoException;
import org.kurento.jsonrpc.TransportException;
import org.kurento.jsonrpc.internal.JsonRpcConstants;
import org.kurento.jsonrpc.internal.JsonRpcRequestSenderHelper;
import org.kurento.jsonrpc.internal.client.ClientSession;
import org.kurento.jsonrpc.internal.client.TransactionImpl.ResponseSender;
import org.kurento.jsonrpc.internal.ws.PendingRequests;
import org.kurento.jsonrpc.internal.ws.WebSocketResponseSender;
import org.kurento.jsonrpc.message.MessageUtils;
import org.kurento.jsonrpc.message.Request;
import org.kurento.jsonrpc.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonRpcClientWebSocket extends JsonRpcClient {

	private final Logger log = LoggerFactory
			.getLogger(JsonRpcClientWebSocket.class);

	private ExecutorService execService = Executors.newFixedThreadPool(10);

	private String url;
	private volatile WebSocketSession wsSession;
	private final PendingRequests pendingRequests = new PendingRequests();
	private final HttpHeaders headers = new HttpHeaders();
	private ResponseSender rs;

	private JsonRpcWSConnectionListener connectionListener;

	private boolean clientClose = false;

	private static final long TIMEOUT = 10000;

	public JsonRpcClientWebSocket(String url) {
		this(url, new HttpHeaders(), null);
	}

	public JsonRpcClientWebSocket(String url, HttpHeaders headers,
			JsonRpcWSConnectionListener connectionListener) {

		this.url = url;
		this.connectionListener = connectionListener;

		rsHelper = new JsonRpcRequestSenderHelper() {
			@Override
			public <P, R> Response<R> internalSendRequest(Request<P> request,
					Class<R> resultClass) throws IOException {

				return internalSendRequestWebSocket(request, resultClass);
			}

			@Override
			protected void internalSendRequest(
					Request<? extends Object> request,
					Class<JsonElement> resultClass,
					Continuation<Response<JsonElement>> continuation) {

				internalSendRequestWebSocket(request, resultClass, continuation);
			}
		};

		if (headers != null) {
			this.headers.putAll(headers);
		}
	}

	public JsonRpcClientWebSocket(String url,
			JsonRpcWSConnectionListener connectionListener) {
		this(url, new HttpHeaders(), connectionListener);
	}

	protected void internalSendRequestWebSocket(
			final Request<? extends Object> request,
			final Class<JsonElement> resultClass,
			final Continuation<Response<JsonElement>> continuation) {

		// FIXME: Poor man async implementation.
		execService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					Response<JsonElement> result = internalSendRequestWebSocket(
							request, resultClass);
					try {
						continuation.onSuccess(result);
					} catch (Exception e) {
						log.error("Exception while processing response", e);
					}
				} catch (Exception e) {
					continuation.onError(e);
				}
			}
		});
	}

	public synchronized void connectIfNecessary() throws IOException {

		if (wsSession == null || !wsSession.isOpen()) {

			final CountDownLatch latch = new CountDownLatch(1);

			TextWebSocketHandler webSocketHandler = new TextWebSocketHandler() {

				@Override
				public void afterConnectionEstablished(
						WebSocketSession wsSession2) throws Exception {

					wsSession = wsSession2;
					rs = new WebSocketResponseSender(wsSession);
					latch.countDown();
					if (connectionListener != null) {
						connectionListener.connected();
					}
				}

				@Override
				public void handleTextMessage(WebSocketSession session,
						TextMessage message) throws Exception {
					handleWebSocketTextMessage(message);
				}

				@Override
				public void afterConnectionClosed(WebSocketSession s,
						CloseStatus status) throws Exception {
					handleReconnectDisconnection(s, status);
				}
			};

			WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(
					new StandardWebSocketClient(), webSocketHandler, url);
			connectionManager.setHeaders(headers);
			connectionManager.start();

			try {
				// FIXME: Make this configurable and search a way to detect the
				// underlying connection timeout
				if (!latch.await(10, TimeUnit.SECONDS)) {
					if (connectionListener != null) {
						connectionListener.connectionTimeout();
					}
					throw new KurentoException(
							"Timeout of 10s when waiting to connect to Websocket server");
				}

				if (session == null) {

					session = new ClientSession(null, null,
							JsonRpcClientWebSocket.this);
					handlerManager.afterConnectionEstablished(session);

				} else {

					String result = rsHelper.sendRequest(
							JsonRpcConstants.METHOD_RECONNECT, String.class);

					log.info("Reconnection result: {}", result);

				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	protected void handleReconnectDisconnection(final WebSocketSession s,
			final CloseStatus status) {

		if (!clientClose) {

			execService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						connectIfNecessary();
					} catch (KurentoException e) {

						handlerManager.afterConnectionClosed(session,
								status.getReason());

						log.debug("WebSocket closed due to: {}", status);
						wsSession = null;

						if (connectionListener != null) {
							connectionListener.disconnected();
						}

					} catch (IOException e) {
						log.warn("Exception trying to reconnect", e);
					}
				}
			});

			clientClose = false;

		} else {
			if (connectionListener != null) {
				connectionListener.disconnected();
			}
		}
	}

	private void handleWebSocketTextMessage(TextMessage message)
			throws IOException {

		JsonObject jsonMessage = fromJson(message.getPayload(),
				JsonObject.class);

		if (jsonMessage.has(JsonRpcConstants.METHOD_PROPERTY)) {
			handleRequestFromServer(jsonMessage);
		} else {
			handleResponseFromServer(jsonMessage);
		}
	}

	private void handleRequestFromServer(final JsonObject message)
			throws IOException {

		// TODO: Think better ways to do this:
		// handleWebSocketTextMessage seems to be sequential. That is, the
		// message waits to be processed until previous message is being
		// processed. This behavior doesn't allow made a new request in the
		// handler of an event. To avoid this problem, we have decided to
		// process requests from server in a new thread (reused from
		// ExecutorService).
		execService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					handlerManager.handleRequest(session,
							fromJsonRequest(message, JsonElement.class), rs);
				} catch (IOException e) {
					log.warn("Exception processing request " + message, e);
				}
			}
		});
	}

	private void handleResponseFromServer(JsonObject message) {

		Response<JsonElement> response = fromJsonResponse(message,
				JsonElement.class);

		setSessionId(response.getSessionId());

		pendingRequests.handleResponse(response);
	}

	private <P, R> Response<R> internalSendRequestWebSocket(Request<P> request,
			Class<R> resultClass) throws IOException {

		connectIfNecessary();

		Future<Response<JsonElement>> responseFuture = null;

		if (request.getId() != null) {
			responseFuture = pendingRequests.prepareResponse(request.getId());
		}

		String jsonMessage = request.toString();
		log.debug("Req-> {}", jsonMessage.trim());
		synchronized (wsSession) {
			wsSession.sendMessage(new TextMessage(jsonMessage));
		}

		if (responseFuture == null) {
			return null;
		}

		Response<JsonElement> responseJson;
		try {

			responseJson = responseFuture.get(TIMEOUT, TimeUnit.MILLISECONDS);

			log.debug("<-Res {}", responseJson.toString());

			Response<R> response = MessageUtils.convertResponse(responseJson,
					resultClass);

			if (response.getSessionId() != null) {
				session.setSessionId(response.getSessionId());
			}

			return response;

		} catch (InterruptedException e) {
			// TODO What to do in this case?
			throw new KurentoException(
					"Interrupted while waiting for a response", e);
		} catch (ExecutionException e) {
			// TODO Is there a better way to handle this?
			throw new KurentoException("This exception shouldn't be thrown", e);
		} catch (TimeoutException e) {
			throw new TransportException("Timeout of " + TIMEOUT
					+ " seconds waiting from response", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (wsSession != null) {
			clientClose = true;
			wsSession.close();
		}
	}

	public WebSocketSession getWebSocketSession() {
		return wsSession;
	}
}
