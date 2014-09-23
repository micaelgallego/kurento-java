package org.kurento.client.internal.client;

import java.lang.reflect.Type;

import org.kurento.client.Continuation;
import org.kurento.client.internal.transport.serialization.ParamsFlattener;
import org.kurento.jsonrpc.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class RemoteObject {

	private static Logger LOG = LoggerFactory
			.getLogger(RemoteObject.class);

	private static ParamsFlattener FLATTENER = ParamsFlattener.getInstance();

	public interface RemoteObjectEventListener {
		public void onEvent(String eventType, Props data);
	}

	private final String objectRef;
	private final RomManager manager;
	private final String type;

	// This object is used in the process of unflatten. It is common that
	// RemoteObject is used with a Typed wrapper (with reflexion, with code
	// generation or by hand). In this cases, the object reference is unflatten
	// to this value instead of RemoteObject itself.
	private Object wrapperForUnflatten;

	private final Multimap<String, RemoteObjectEventListener> listeners = Multimaps
			.synchronizedMultimap(ArrayListMultimap
					.<String, RemoteObjectEventListener> create());

	public RemoteObject(String objectRef, String type,
			RomManager manager) {
		this.objectRef = objectRef;
		this.manager = manager;
		this.type = type;

		this.manager.registerObject(objectRef, this);
	}

	public Object getWrapperForUnflatten() {
		return wrapperForUnflatten;
	}

	public void setWrapperForUnflatten(Object wrapperForUnflatten) {
		this.wrapperForUnflatten = wrapperForUnflatten;
	}

	@SuppressWarnings("unchecked")
	public <E> E invoke(String method, Props params, Class<E> clazz) {
		return (E) invoke(method, params, (Type) clazz);
	}

	public Object invoke(String method, Props params, Type type) {

		Type flattenType = FLATTENER.calculateFlattenType(type);

		Object obj = manager.invoke(objectRef, method, params, flattenType);

		return FLATTENER.unflattenValue("return", type, obj, manager);
	}

	@SuppressWarnings("rawtypes")
	public void invoke(String method, Props params, final Type type,
			final Continuation cont) {

		Type flattenType = FLATTENER.calculateFlattenType(type);

		manager.invoke(objectRef, method, params, flattenType,
				new DefaultContinuation<Object>(cont) {
					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(Object result) {
						try {
							cont.onSuccess(FLATTENER.unflattenValue("return",
									type, result, manager));
						} catch (Exception e) {
							log.warn(
									"[Continuation] error invoking onSuccess implemented by client",
									e);
						}
					}
				});
	}

	public void release() {
		manager.release(objectRef);
	}

	public void release(final Continuation<Void> cont) {
		manager.release(objectRef, cont);
	}

	public ListenerSubscriptionImpl addEventListener(String eventType,
			RemoteObjectEventListener listener) {

		String subscription = manager.subscribe(objectRef, eventType);

		listeners.put(eventType, listener);

		return new ListenerSubscriptionImpl(subscription, eventType, listener);
	}

	public void addEventListener(final String eventType,
			final Continuation<ListenerSubscriptionImpl> cont,
			final RemoteObjectEventListener listener) {

		manager.subscribe(objectRef, eventType, new DefaultContinuation<String>(
				cont) {
			@Override
			public void onSuccess(String subscription) {
				listeners.put(eventType, listener);
				try {
					cont.onSuccess(new ListenerSubscriptionImpl(subscription,
							eventType, listener));
				} catch (Exception e) {
					log.warn(
							"[Continuation] error invoking onSuccess implemented by client",
							e);
				}
			}
		});
	}

	public String getObjectRef() {
		return objectRef;
	}

	public void fireEvent(String type, Props data) {
		for (RemoteObjectEventListener eventListener : this.listeners.get(type)) {
			try {
				eventListener.onEvent(type, data);
			} catch (Exception e) {
				LOG.error("Exception executing event listener", e);
			}
		}
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objectRef == null) ? 0 : objectRef.hashCode());
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
		RemoteObject other = (RemoteObject) obj;
		if (objectRef == null) {
			if (other.objectRef != null) {
				return false;
			}
		} else if (!objectRef.equals(other.objectRef)) {
			return false;
		}
		return true;
	}

}
