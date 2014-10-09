package org.kurento.client.internal.client;

import java.lang.reflect.Type;

import org.kurento.client.Continuation;
import org.kurento.client.internal.transport.serialization.ObjectRefsManager;
import org.kurento.jsonrpc.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RomManager implements ObjectRefsManager {

	private static final Logger log = LoggerFactory.getLogger(RomManager.class);

	private final RomClientObjectManager manager;
	private final RomClient client;

	public RomManager(RomClient client) {
		this.client = client;
		this.manager = new RomClientObjectManager(this);
		if (client != null) {
			this.client.addRomEventHandler(manager);
		}
	}

	public RemoteObject create(String remoteClassName, Props constructorParams) {

		String objectRef = client.create(remoteClassName, constructorParams);
		return new RemoteObject(objectRef, remoteClassName, this);
	}

	public RemoteObject create(String remoteClassName) {
		return create(remoteClassName, (Props) null);
	}

	public void create(final String remoteClassName,
			final Props constructorParams, final Continuation<RemoteObject> cont) {

		client.create(remoteClassName, constructorParams,
				new Continuation<String>() {
					@Override
					public void onSuccess(String objectRef) {
						try {
							cont.onSuccess(new RemoteObject(objectRef,
									remoteClassName, RomManager.this));
						} catch (Exception e) {
							log.warn(
									"[Continuation] error invoking onSuccess implemented by client",
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
	}

	public void create(String remoteClassName, Continuation<RemoteObject> cont) {
		create(remoteClassName, null, cont);
	}

	public void destroy() {
		this.client.destroy();
	}

	public <E> E invoke(String objectRef, String methodName, Props params,
			Class<E> clazz) {
		return client.invoke(objectRef, methodName, params, clazz);
	}

	public Object invoke(String objectRef, String operationName,
			Props operationParams, Type type) {
		return client.invoke(objectRef, operationName, operationParams, type);
	}

	public void release(String objectRef) {
		client.release(objectRef);
		manager.releaseObject(objectRef);
	}

	public String subscribe(String objectRef, String eventType) {
		return client.subscribe(objectRef, eventType);
	}

	public Object invoke(String objectRef, String operationName,
			Props operationParams, Type type, Continuation<?> cont) {
		return client.invoke(objectRef, operationName, operationParams, type,
				cont);
	}

	public void release(final String objectRef, final Continuation<Void> cont) {
		client.release(objectRef, new DefaultContinuation<Void>(cont) {
			@Override
			public void onSuccess(Void result) {
				manager.releaseObject(objectRef);
				try {
					cont.onSuccess(null);
				} catch (Exception e) {
					log.warn(
							"[Continuation] error invoking onSuccess implemented by client",
							e);
				}
			}
		});
	}

	public String subscribe(String objectRef, String type,
			Continuation<String> cont) {
		return client.subscribe(objectRef, type, cont);
	}

	public void addRomEventHandler(RomEventHandler eventHandler) {
		client.addRomEventHandler(eventHandler);
	}

	@Override
	public Object getObject(String objectRef) {
		return manager.getObject(objectRef);
	}

	public void registerObject(String objectRef, RemoteObject remoteObject) {
		this.manager.registerObject(objectRef, remoteObject);
	}

	public RomClientObjectManager getObjectManager() {
		return manager;
	}
}
