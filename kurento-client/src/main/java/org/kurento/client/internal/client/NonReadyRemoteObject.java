package org.kurento.client.internal.client;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.Continuation;
import org.kurento.client.MediaObjectNonReadyException;
import org.kurento.client.MediaPipelineNotStartedException;
import org.kurento.client.TransactionNotExecutedException;
import org.kurento.client.internal.client.RemoteObject.RemoteObjectEventListener;
import org.kurento.client.internal.client.operation.InvokeOperation;
import org.kurento.client.internal.client.operation.SubscriptionOperation;
import org.kurento.jsonrpc.Props;

public class NonReadyRemoteObject implements RemoteObjectFacade {

	public enum NonReadyMode {
		CREATION, TRANSACTION
	};

	private static final Set<String> NON_READY_VALID_METHODS = new HashSet<>(
			Arrays.asList("connect"));

	private AbstractMediaObject mediaObject;
	private NonReadyMode nonReadyMode;

	public NonReadyRemoteObject(AbstractMediaObject mediaObject,
			NonReadyMode nonReadyMode) {
		this.mediaObject = mediaObject;
		this.nonReadyMode = nonReadyMode;
	}

	public NonReadyRemoteObject() {
		this.nonReadyMode = NonReadyMode.CREATION;
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

		switch (nonReadyMode) {
		case CREATION:
			if (NON_READY_VALID_METHODS.contains(method)) {

				mediaObject.getInternalMediaPipeline()
						.addOperation(
								new InvokeOperation((AbstractMediaObject) this
										.getPublicObject(), method, params,
										returnType));

				// TODO Only non-return value methods are allowed
				return null;
			} else {
				throw getException();
			}

		case TRANSACTION:
			throw new TransactionNotExecutedException();
		default:
			throw new Error(
					"Programming error. NonReadyRemoteObject is in invalid mode: "
							+ nonReadyMode);
		}
	}

	@Override
	public void invoke(String method, Props params, Type type, Continuation cont) {

		switch (nonReadyMode) {
		case CREATION:
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
					cont.onError(getException());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case TRANSACTION:
			try {
				cont.onError(getException());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			throw new Error(
					"Programming error. NonReadyRemoteObject is in invalid mode: "
							+ nonReadyMode);
		}
	}

	@Override
	public void release() {
		throwException();
	}

	@Override
	public void release(Continuation<Void> cont) {
		try {
			cont.onError(getException());
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
			RemoteObjectEventListener listener,
			Continuation<ListenerSubscriptionImpl> cont) {
		try {
			cont.onError(getException());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getObjectRef() {
		throwException();
		return null;
	}

	@Override
	public void fireEvent(String type, Props data) {
		throwException();
	}

	@Override
	public String getType() {
		throwException();
		return null;
	}

	@Override
	public RomManager getRomManager() {
		throwException();
		return null;
	}

	private void throwException() {
		throw getException();
	}

	private MediaObjectNonReadyException getException() {
		switch (nonReadyMode) {
		case CREATION:
			return new MediaPipelineNotStartedException();
		case TRANSACTION:
			return new TransactionNotExecutedException();
		default:
			break;
		}
		return new MediaPipelineNotStartedException();
	}
}
