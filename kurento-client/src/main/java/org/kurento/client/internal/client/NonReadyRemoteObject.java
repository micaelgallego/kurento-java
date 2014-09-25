package org.kurento.client.internal.client;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RemoteObject.RemoteObjectEventListener;
import org.kurento.client.internal.client.operation.InvokeOperation;
import org.kurento.client.internal.client.operation.SubscriptionOperation;
import org.kurento.jsonrpc.Props;

public class NonReadyRemoteObject implements RemoteObjectFacade {

	private static final Set<String> NON_READY_VALID_METHODS = new HashSet<>(
			Arrays.asList("connect"));

	private AbstractMediaObject mediaObject;

	public NonReadyRemoteObject(AbstractMediaObject mediaObject) {
		this.mediaObject = mediaObject;
	}

	public NonReadyRemoteObject() {
	}

	@Override
	public AbstractMediaObject getPublicObject() {
		return mediaObject;
	}

	@Override
	public void setPublicObject(AbstractMediaObject mediaObject) {
		this.mediaObject = mediaObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E invoke(String method, Props params, Class<E> clazz) {
		return (E) invoke(method, params, (Type) clazz);
	}

	@Override
	public Object invoke(String method, Props params, Type returnType) {

		if (NON_READY_VALID_METHODS.contains(method)) {

			mediaObject.getInternalMediaPipeline().addOperation(
					new InvokeOperation((AbstractMediaObject) this
							.getPublicObject(), method, params, returnType));

			// TODO Only non-return value methods are allowed
			return null;
		} else {
			throw new MediaPipelineNotStartedException();
		}
	}

	@Override
	public void invoke(String method, Props params, Type type, Continuation cont) {

		if (NON_READY_VALID_METHODS.contains(method)) {

			mediaObject.getInternalMediaPipeline().addOperation(
					new InvokeOperation((AbstractMediaObject) this
							.getPublicObject(), method, params, type));

			// TODO Only non-return value methods are allowed
			try {
				cont.onSuccess(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				cont.onError(new MediaPipelineNotStartedException());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void release() {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void release(Continuation<Void> cont) {
		try {
			cont.onError(new MediaPipelineNotStartedException());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ListenerSubscriptionImpl addEventListener(String eventType,
			RemoteObjectEventListener listener) {

		SubscriptionOperation op = new SubscriptionOperation(
				(AbstractMediaObject) this.getPublicObject(), eventType,
				listener);

		mediaObject.getInternalMediaPipeline().addOperation(op);

		return op.getListenerSubscription();
	}

	@Override
	public void addEventListener(String eventType,
			Continuation<ListenerSubscriptionImpl> cont,
			RemoteObjectEventListener listener) {
		try {
			cont.onError(new MediaPipelineNotStartedException());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getObjectRef() {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void fireEvent(String type, Props data) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public String getType() {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public RomManager getRomManager() {
		throw new MediaPipelineNotStartedException();
	}
}
