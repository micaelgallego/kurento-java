package org.kurento.client.internal.client;

import java.lang.reflect.Type;

import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RemoteObject.RemoteObjectEventListener;
import org.kurento.jsonrpc.Props;

public class RemoteObjectDef implements RemoteObjectFacade {

	@Override
	public Object getWrapperForUnflatten() {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void setWrapperForUnflatten(Object wrapperForUnflatten) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public <E> E invoke(String method, Props params, Class<E> clazz) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public Object invoke(String method, Props params, Type type) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void invoke(String method, Props params, Type type, Continuation cont) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void release() {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void release(Continuation<Void> cont) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public ListenerSubscriptionImpl addEventListener(String eventType,
			RemoteObjectEventListener listener) {
		throw new MediaPipelineNotStartedException();
	}

	@Override
	public void addEventListener(String eventType,
			Continuation<ListenerSubscriptionImpl> cont,
			RemoteObjectEventListener listener) {
		throw new MediaPipelineNotStartedException();
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
