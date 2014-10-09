package org.kurento.client.internal.client;

import java.lang.reflect.Type;

import org.kurento.client.Continuation;
import org.kurento.jsonrpc.Props;

public interface RomClient {

	// Sync methods --------------------------------------

	public abstract String create(String remoteClassName,
			Props constructorParams);

	public abstract <E> E invoke(String objectRef, String methodName,
			Props params, Class<E> clazz);

	public abstract Object invoke(String objectRef, String operationName,
			Props operationParams, Type type);

	public abstract void release(String objectRef);

	public abstract String subscribe(String objectRef, String eventType);

	// Async methods --------------------------------------

	public abstract String create(String remoteClassName,
			Props constructorParams, Continuation<String> cont);

	public abstract Object invoke(String objectRef, String operationName,
			Props operationParams, Type type, Continuation<?> cont);

	public abstract void release(String objectRef, Continuation<Void> cont);

	public abstract String subscribe(String objectRef, String type,
			Continuation<String> cont);

	// Other methods --------------------------------------

	public abstract void addRomEventHandler(RomEventHandler eventHandler);

	public abstract void destroy();

}
