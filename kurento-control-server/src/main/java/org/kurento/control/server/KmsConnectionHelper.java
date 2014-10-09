package org.kurento.control.server;

import static org.kurento.commons.PropertiesManager.getProperty;

import java.lang.reflect.Constructor;

import org.kurento.jsonrpc.client.JsonRpcClient;
import org.kurento.jsonrpc.client.JsonRpcClientWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KmsConnectionHelper {

	public static final String KMS_WEBSOCKETS_HOST_PROPERTY = "mediaServer.net.websocket.host";
	public static final String KMS_WEBSOCKETS_PORT_PROPERTY = "mediaServer.net.websocket.port";
	public static final String KMS_WEBSOCKETS_PATH_PROPERTY = "mediaServer.net.websocket.path";

	public static final String KMS_WEBSOCKETS_HOST_DEFAULT = "localhost";
	public static final String KMS_WEBSOCKETS_PORT_DEFAULT = "8888";
	public static final String KMS_WEBSOCKETS_PATH_DEFAULT = "kurento";

	public static final String RABBITMQ_PORT_PROPERTY = "mediaServer.net.rabbitmq.port";
	public static final String RABBITMQ_HOST_PROPERTY = "mediaServer.net.rabbitmq.host";
	public static final String RABBITMQ_USERNAME_PROPERTY = "mediaServer.net.rabbitmq.username";
	public static final String RABBITMQ_PASSWORD_PROPERTY = "mediaServer.net.rabbitmq.password";
	public static final String RABBITMQ_VHOST_PROPERTY = "mediaServer.net.rabbitmq.vhost";

	public static final String RABBITMQ_PORT_DEFAULT = "5672";
	public static final String RABBITMQ_HOST_DEFAULT = "127.0.0.1";
	public static final String RABBITMQ_USERNAME_DEFAULT = "guest";
	public static final String RABBITMQ_PASSWORD_DEFAULT = "guest";
	public static final String RABBITMQ_VHOST_DEFAULT = "/";

	private static final Logger log = LoggerFactory
			.getLogger(KmsConnectionHelper.class);

	public static JsonRpcClient createJsonRpcClient() {

		String rabbitMqHost = getProperty(RABBITMQ_HOST_PROPERTY);
		if (rabbitMqHost != null) {

			String port = getProperty(RABBITMQ_PORT_PROPERTY,
					RABBITMQ_PORT_DEFAULT);
			String host = getProperty(RABBITMQ_HOST_PROPERTY,
					RABBITMQ_HOST_DEFAULT);
			String username = getProperty(RABBITMQ_USERNAME_PROPERTY,
					RABBITMQ_USERNAME_DEFAULT);
			String password = getProperty(RABBITMQ_PASSWORD_PROPERTY,
					RABBITMQ_PASSWORD_DEFAULT);
			String vhost = getProperty(RABBITMQ_VHOST_PROPERTY,
					RABBITMQ_VHOST_DEFAULT);

			log.info("Kurento Control Server using RabbitMQ to communicate wiht Kurento Media Server.");

			return newJsonRpcClientRabbitMq(host, port, username, password,
					vhost);
		}

		String host = getProperty(KMS_WEBSOCKETS_HOST_PROPERTY,
				KMS_WEBSOCKETS_HOST_DEFAULT);
		String port = getProperty(KMS_WEBSOCKETS_PORT_PROPERTY,
				KMS_WEBSOCKETS_PORT_DEFAULT);
		String path = getProperty(KMS_WEBSOCKETS_PATH_PROPERTY,
				KMS_WEBSOCKETS_PATH_DEFAULT);

		String wsUri = "ws://" + host + ":" + port + "/" + path;

		log.info("Kurento Control Server using ws to communicate wiht Kurento Media Server.");
		log.info("KMS ws uri: " + wsUri);

		return new JsonRpcClientWebSocket(wsUri);

	}

	private static JsonRpcClient newJsonRpcClientRabbitMq(String host,
			String port, String username, String password, String vhost) {

		try {

			@SuppressWarnings("unchecked")
			Class<? extends JsonRpcClient> clazz = (Class<? extends JsonRpcClient>) Class
					.forName("org.kurento.rabbitmq.client.JsonRpcClientRabbitMq");

			Constructor<? extends JsonRpcClient> constructor = clazz
					.getConstructor(String.class, String.class, String.class,
							String.class, String.class);

			return constructor.newInstance(host, port, username, password,
					vhost);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"KurentoClient is configured to use RabbitMQ but class "
							+ "JsonRpcClientRabbitMq is not in the classpath. Plase review "
							+ "you have correctly configured the dependency with kurento-rabbitmq project.",
					e);

		} catch (Exception e) {
			throw new RuntimeException(
					"Exception creating JsonRpcClientRabbitMq", e);
		}
	}

}
