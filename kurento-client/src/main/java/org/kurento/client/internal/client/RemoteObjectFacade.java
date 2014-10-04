package org.kurento.client.internal.client;

import java.lang.reflect.Type;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RemoteObject.RemoteObjectEventListener;
import org.kurento.jsonrpc.Props;

public interface RemoteObjectFacade {

	public abstract AbstractMediaObject getPublicObject();

	public abstract void setPublicObject(AbstractMediaObject object);

	public abstract <E> E invoke(String method, Props params, Class<E> clazz);

	public abstract Object invoke(String method, Props params, Type type);

	public abstract void invoke(String method, Props params, Type type,
			Continuation<?> cont);

	public abstract void release();

	public abstract void release(Continuation<Void> cont);

	public abstract ListenerSubscriptionImpl addEventListener(String eventType,
			RemoteObjectEventListener listener);

	public abstract void addEventListener(String eventType,
			RemoteObjectEventListener listener,
			Continuation<ListenerSubscriptionImpl> cont);

	public abstract String getObjectRef();

	public abstract void fireEvent(String type, Props data);

	public abstract String getType();

	public abstract RomManager getRomManager();

}