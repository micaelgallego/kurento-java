package org.kurento.client;

import java.lang.reflect.Constructor;

import org.kurento.client.internal.ParamAnnotationUtils;
import org.kurento.client.internal.client.ListenerSubscriptionImpl;
import org.kurento.client.internal.client.RemoteObject;
import org.kurento.client.internal.client.RemoteObjectFactory;
import org.kurento.jsonrpc.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractMediaObject {

	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractMediaObject.class);

	protected final RemoteObject remoteObject;
	protected final RemoteObjectFactory factory;

	protected AbstractMediaObject(RemoteObject remoteObject,
			RemoteObjectFactory factory) {
		this.remoteObject = remoteObject;
		this.remoteObject.setWrapperForUnflatten(this);
		this.factory = factory;
	}

	// private Object invoke(Method method, Object[] args, Continuation<?> cont)
	// {
	//
	// Props props = ParamAnnotationUtils.extractProps(
	// method.getParameterAnnotations(), args);
	//
	// if (cont != null) {
	//
	// Type[] paramTypes = method.getGenericParameterTypes();
	// ParameterizedType contType = (ParameterizedType)
	// paramTypes[paramTypes.length - 1];
	// Type returnType = contType.getActualTypeArguments()[0];
	// remoteObject.invoke(method.getName(), props, returnType, cont);
	// return null;
	// }
	//
	// return remoteObject.invoke(method.getName(), props,
	// method.getGenericReturnType());
	// }

	protected ListenerSubscription subscribeEventListener(
			final EventListener<?> clientListener,
			final Class<? extends Event> eventClass, Continuation<?> cont) {

		String eventName = eventClass.getSimpleName().substring(0,
				eventClass.getSimpleName().length() - "Event".length());

		RemoteObject.RemoteObjectEventListener listener = new RemoteObject.RemoteObjectEventListener() {
			@Override
			public void onEvent(String eventType, Props data) {
				propagateEventTo(AbstractMediaObject.this, eventClass, data,
						clientListener);
			}
		};

		if (cont != null) {
			remoteObject.addEventListener(eventName,
					(Continuation<ListenerSubscriptionImpl>) cont, listener);
			return null;
		} else {
			return remoteObject.addEventListener(eventName, listener);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void propagateEventTo(Object object,
			Class<? extends Event> eventClass, Props data,
			EventListener<?> listener) {

		// TODO Optimize this to create only one event for all listeners

		try {

			Constructor<?> constructor = eventClass.getConstructors()[0];

			Object[] params = ParamAnnotationUtils.extractEventParams(
					constructor.getParameterAnnotations(), data);

			params[0] = object;

			Event e = (Event) constructor.newInstance(params);

			((EventListener) listener).onEvent(e);

		} catch (Exception e) {
			LOG.error(
					"Exception while processing event '"
							+ eventClass.getSimpleName() + "' with params '"
							+ data + "'", e);
		}
	}

	public RemoteObject getRemoteObject() {
		return remoteObject;
	}

	public RemoteObjectFactory getFactory() {
		return factory;
	}

	@Override
	public String toString() {
		return "[MediaObject: type=" + this.remoteObject.getType()
				+ " remoteRef=" + remoteObject.getObjectRef() + "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((remoteObject == null) ? 0 : remoteObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractMediaObject other = (AbstractMediaObject) obj;
		if (remoteObject == null) {
			if (other.remoteObject != null) {
				return false;
			}
		} else if (!remoteObject.equals(other.remoteObject)) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * Explicitly release a media object form memory. All of its children will
	 * also be released.
	 *
	 **/
	public void release() {
		release(null);
	}

	/**
	 *
	 * Explicitly release a media object form memory. All of its children will
	 * also be released. Asynchronous call.
	 *
	 * @param continuation
	 *            {@link #onSuccess(void)} will be called when the actions
	 *            complete. {@link #onError} will be called if there is an
	 *            exception.
	 *
	 **/
	@SuppressWarnings("unchecked")
	public Object release(Continuation<?> cont) {
		if (cont != null) {
			remoteObject.release((Continuation<Void>) cont);
		} else {
			remoteObject.release();
		}
		return null;
	}

}
