package org.kurento.client.internal.transport.jsonrpc;

import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.CREATE_CONSTRUCTOR_PARAMS;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.CREATE_METHOD;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.CREATE_TYPE;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.INVOKE_METHOD;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.INVOKE_OBJECT;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.INVOKE_OPERATION_NAME;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.INVOKE_OPERATION_PARAMS;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.ONEVENT_DATA;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.ONEVENT_OBJECT;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.ONEVENT_SUBSCRIPTION;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.ONEVENT_TYPE;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.RELEASE_METHOD;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.RELEASE_OBJECT;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.SUBSCRIBE_METHOD;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.SUBSCRIBE_OBJECT;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.SUBSCRIBE_TYPE;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.TRANSACTION_METHOD;
import static org.kurento.client.internal.transport.jsonrpc.RomJsonRpcConstants.TRANSACTION_OPERATIONS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RomClient;
import org.kurento.client.internal.client.RomEventHandler;
import org.kurento.client.internal.client.operation.Operation;
import org.kurento.client.internal.server.KurentoServerException;
import org.kurento.client.internal.server.KurentoServerTransportException;
import org.kurento.client.internal.transport.serialization.ParamsFlattener;
import org.kurento.jsonrpc.DefaultJsonRpcHandler;
import org.kurento.jsonrpc.JsonRpcErrorException;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.jsonrpc.Props;
import org.kurento.jsonrpc.Transaction;
import org.kurento.jsonrpc.client.JsonRpcClient;
import org.kurento.jsonrpc.message.Request;
import org.kurento.jsonrpc.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RomClientJsonRpcClient implements RomClient {

	public class RequestAndResponseType {

		public Request<JsonObject> request;
		public Type responseType;

		public RequestAndResponseType(Request<JsonObject> request,
				Type responseType) {
			this.request = request;
			this.responseType = responseType;
		}
	}

	private static final Logger log = LoggerFactory
			.getLogger(RomClientJsonRpcClient.class);

	private final JsonRpcClient client;

	public RomClientJsonRpcClient(JsonRpcClient client) {
		this.client = client;
	}

	// Sync operations

	@Override
	public Object invoke(String objectRef, String operationName,
			Props operationParams, Type type) {
		return invoke(objectRef, operationName, operationParams, type, null);
	}

	@Override
	public String subscribe(String objectRef, String type) {
		return subscribe(objectRef, type, null);
	}

	@Override
	public String create(String remoteClassName, Props constructorParams) {
		return create(remoteClassName, constructorParams, null);
	}

	@Override
	public void release(String objectRef) {
		release(objectRef, null);
	}

	// Async operations

	@Override
	public String create(String remoteClassName, Props constructorParams,
			Continuation<String> cont) {

		RequestAndResponseType reqres = createCreateRequest(remoteClassName,
				constructorParams);

		return this.<String, String> sendRequest(reqres.request,
				reqres.responseType, null, cont);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E invoke(String objectRef, String operationName,
			Props operationParams, Class<E> clazz) {

		return (E) invoke(objectRef, operationName, operationParams,
				(Type) clazz);
	}

	@Override
	public Object invoke(String objectRef, String operationName,
			Props operationParams, Type type, Continuation<?> cont) {

		RequestAndResponseType reqres = createInvokeRequest(objectRef,
				operationName, operationParams, type);

		return sendRequest(reqres.request, reqres.responseType, null, cont);
	}

	@Override
	public void release(String objectRef, Continuation<Void> cont) {

		RequestAndResponseType reqres = createReleaseRequest(objectRef);
		sendRequest(reqres.request, reqres.responseType, null, cont);
	}

	@Override
	public String subscribe(String objectRef, String eventType,
			Continuation<String> cont) {

		RequestAndResponseType reqres = createSubscribeRequest(objectRef,
				eventType);

		return sendRequest(reqres.request, reqres.responseType, null, cont);
	}

	// Other methods

	@Override
	public void addRomEventHandler(final RomEventHandler eventHandler) {

		this.client
				.setServerRequestHandler(new DefaultJsonRpcHandler<JsonObject>() {

					@Override
					public void handleRequest(Transaction transaction,
							Request<JsonObject> request) throws Exception {
						processEvent(eventHandler, request);
					}
				});
	}

	private void processEvent(RomEventHandler eventHandler,
			Request<JsonObject> request) {

		JsonObject params = request.getParams();

		try {
			params = (JsonObject) params.get("value");
		} catch (Exception e) {
		}

		String objectRef = params.get(ONEVENT_OBJECT).getAsString();
		String subscription = "";
		if (params.has(ONEVENT_SUBSCRIPTION)) {
			subscription = params.get(ONEVENT_SUBSCRIPTION).getAsString();
		}
		String type = params.get(ONEVENT_TYPE).getAsString();
		JsonObject jsonData = (JsonObject) params.get(ONEVENT_DATA);
		Props data = JsonUtils.fromJson(jsonData, Props.class);

		eventHandler.processEvent(objectRef, subscription, type, data);
	}

	@Override
	public void destroy() {
		try {
			client.close();
		} catch (IOException e) {
			throw new RuntimeException("Exception while closing JsonRpcClient",
					e);
		}
	}

	@SuppressWarnings("unchecked")
	private <P, R> R sendRequest(Request<JsonObject> request,
			final Type responseType, final Function<P, R> processor,
			final Continuation<R> cont) {

		try {

			if (cont == null) {

				return processReqResult(
						responseType,
						processor,
						client.sendRequest(request.getMethod(),
								request.getParams(), JsonElement.class));

			}

			client.sendRequest(request.getMethod(), request.getParams(),
					new org.kurento.jsonrpc.client.Continuation<JsonElement>() {

						@SuppressWarnings({ "rawtypes" })
						@Override
						public void onSuccess(JsonElement reqResult) {

							R methodResult = processReqResult(responseType,
									processor, reqResult);
							try {
								((Continuation) cont).onSuccess(methodResult);
							} catch (Exception e) {
								log.warn(
										"[Continuation] error invoking OnSuccess implemented by client",
										e);
							}
						}

						@Override
						public void onError(Throwable cause) {
							try {
								cont.onError(cause);
							} catch (Exception e) {
								log.warn(
										"[Continuation] error invoking onError implemented by client",
										e);
							}
						}
					});

			return null;

		} catch (IOException e) {
			throw new KurentoServerTransportException(
					"Error connecting with server", e);
		} catch (JsonRpcErrorException e) {
			throw new KurentoServerException(
					"Exception invoking an operation in KMS", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <P, R> R processReqResult(final Type type,
			Function<P, R> processor, JsonElement reqResult) {

		P methodResult = JsonResponseUtils.convertFromResult(reqResult, type);

		if (processor == null) {
			return (R) methodResult;
		} else {
			return processor.apply(methodResult);
		}
	}

	// Create JsonRpc requests

	public RequestAndResponseType createInvokeRequest(String objectRef,
			String operationName, Props operationParams, Type type) {

		JsonObject params = new JsonObject();
		params.addProperty(INVOKE_OBJECT, objectRef);
		params.addProperty(INVOKE_OPERATION_NAME, operationName);

		if (operationParams != null) {

			Props flatParams = ParamsFlattener.getInstance().flattenParams(
					operationParams);

			params.add(INVOKE_OPERATION_PARAMS,
					JsonUtils.toJsonObject(flatParams));
		}

		return new RequestAndResponseType(new Request<JsonObject>(
				INVOKE_METHOD, params), type);
	}

	public RequestAndResponseType createReleaseRequest(String objectRef) {

		JsonObject params = JsonUtils.toJsonObject(new Props(RELEASE_OBJECT,
				objectRef));

		return new RequestAndResponseType(new Request<JsonObject>(
				RELEASE_METHOD, params), Void.class);
	}

	public RequestAndResponseType createCreateRequest(String remoteClassName,
			Props constructorParams) {

		JsonObject params = new JsonObject();
		params.addProperty(CREATE_TYPE, remoteClassName);

		if (constructorParams != null) {
			Props flatParams = ParamsFlattener.getInstance().flattenParams(
					constructorParams);

			params.add(CREATE_CONSTRUCTOR_PARAMS,
					JsonUtils.toJsonObject(flatParams));
		}

		return new RequestAndResponseType(new Request<JsonObject>(
				CREATE_METHOD, params), String.class);
	}

	public RequestAndResponseType createSubscribeRequest(String objectRef,
			String eventType) {

		JsonObject params = JsonUtils.toJsonObject(new Props(SUBSCRIBE_OBJECT,
				objectRef).add(SUBSCRIBE_TYPE, eventType));

		return new RequestAndResponseType(new Request<JsonObject>(
				SUBSCRIBE_METHOD, params), String.class);
	}

	@Override
	@SuppressWarnings("serial")
	public void transaction(List<Operation> operations) {
		// TODO Now this implementation consider Server side support for
		// transactions. Refactor other code to include here the logic to behave
		// different when the server doesn't have transaction support

		JsonArray opJsons = new JsonArray();
		List<RequestAndResponseType> opReqres = new ArrayList<>();

		for (Operation op : operations) {
			RequestAndResponseType reqres = op.createRequest(this);
			opReqres.add(reqres);
			opJsons.add(JsonUtils.toJsonElement(reqres.request));
		}

		JsonObject params = new JsonObject();
		params.add(TRANSACTION_OPERATIONS, opJsons);

		List<Response<JsonElement>> responses = this.sendRequest(
				new Request<JsonObject>(TRANSACTION_METHOD, params),
				new TypeToken<List<Response<JsonElement>>>() {
				}.getType(), null, null);

		for (int i = 0; i < operations.size(); i++) {
			Operation op = operations.get(i);
			Response<JsonElement> response = responses.get(i);
			RequestAndResponseType reqres = opReqres.get(i);
			SettableFuture<?> future = (SettableFuture<?>) op.getFuture();

			if (response.isError()) {
				future.setException(new JsonRpcErrorException(response
						.getError()));
			} else {
				op.processResponse(processReqResult(reqres.responseType, null,
						response.getResult()));
			}
		}
	}

	@Override
	public void transaction(List<Operation> operations,
			Continuation<Void> continuation) {
		// TODO Implement this
		throw new Error("Not yet implemented");
	}

}
