/**
 * This file is generated with Kurento ktool-rom-processor.
 * Please don't edit. Changes shoult go to kms-interface-rom and
 * ktool-rom-processor templates.
 */
package org.kurento.client;

import org.kurento.client.internal.client.DefaultContinuation;
import org.kurento.client.internal.client.NonReadyRemoteObject;
import org.kurento.client.internal.client.NonReadyRemoteObject.NonReadyMode;
import org.kurento.client.internal.client.RemoteObjectFacade;
import org.kurento.client.internal.client.RomManager;
import org.kurento.client.internal.client.operation.MediaObjectCreationOperation;
import org.kurento.jsonrpc.Prop;
import org.kurento.jsonrpc.Props;

/**
 * Kurento Media Builder base interface
 *
 * Builds a {@code <T>} object, either synchronously using {@link #build} or
 * asynchronously using {@link #buildAsync}
 *
 * @param T
 *            the type of object to build
 *
 **/
public abstract class AbstractBuilder<T extends AbstractMediaObject> {

	protected final Props props;
	private RomManager manager;
	private final Class<?> clazz;

	public AbstractBuilder(Class<?> clazz, MediaObject mediaObject) {

		this.props = new Props();
		this.clazz = clazz;
	}

	public AbstractBuilder(Class<?> clazz, RomManager manager) {

		this.props = new Props();
		this.clazz = clazz;
		this.manager = manager;
	}

	/**
	 * Builds an object synchronously using the builder design pattern
	 *
	 * @return <T> The type of object
	 *
	 **/
	public T create() {
		return internalCreate(null);
	}

	/**
	 * Builds an object asynchronously using the builder design pattern.
	 *
	 * The continuation will have {@link Continuation#onSuccess} called when the
	 * object is ready, or {@link Continuation#onError} if an error occurs
	 *
	 * @param continuation
	 *            will be called when the object is built
	 *
	 *
	 **/
	public void create(final Continuation<T> continuation) {
		internalCreate(continuation);
	}

	private T internalCreate(final Continuation<T> continuation) {

		final AbstractMediaObject constObject = obtainConstructorObject();

		T mediaObject;

		DefaultContinuation<RemoteObjectFacade> defaultContinuation = new DefaultContinuation<RemoteObjectFacade>(
				continuation) {
			@Override
			public void onSuccess(RemoteObjectFacade remoteObject) {
				try {
					T newMediaObject = createMediaObjectConst(constObject,
							remoteObject, null);
					continuation.onSuccess(newMediaObject);
				} catch (Exception e) {
					log.warn(
							"[Continuation] error invoking onSuccess implemented by client",
							e);
				}
			}
		};

		if (constObject == null || constObject.isReady()) {

			if (manager == null && constObject != null) {
				manager = constObject.getRomManager();
			}

			if (continuation == null) {

				RemoteObjectFacade remoteObject = manager.create(
						clazz.getSimpleName(), props);

				mediaObject = createMediaObjectConst(constObject, remoteObject,
						null);

			} else {

				manager.create(clazz.getSimpleName(), props,
						defaultContinuation);

				return null;
			}

		} else {

			MediaPipeline pipeline = constObject.getInternalMediaPipeline();

			NonReadyRemoteObject remoteObject = new NonReadyRemoteObject(
					pipeline, NonReadyMode.CREATION);

			mediaObject = createMediaObjectConst(constObject, remoteObject,
					pipeline.getActiveTransaction());

			if (continuation != null) {
				try {
					continuation.onSuccess(mediaObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			MediaObjectCreationOperation op = new MediaObjectCreationOperation(
					clazz.getSimpleName(), props, mediaObject);

			pipeline.addOperation(op);
		}

		return mediaObject;
	}

	@SuppressWarnings("unchecked")
	private T createMediaObjectConst(AbstractMediaObject constObject,
			RemoteObjectFacade remoteObject, Transaction tx) {

		AbstractMediaObject mediaObject = createMediaObject(remoteObject, tx);

		if (constObject != null) {
			mediaObject.setInternalMediaPipeline(constObject
					.getInternalMediaPipeline());
		}

		return (T) mediaObject;
	}

	private AbstractMediaObject obtainConstructorObject() {
		AbstractMediaObject rootObject = null;
		for (Prop prop : props) {
			Object value = prop.getValue();
			if (value instanceof MediaPipeline || value instanceof Hub) {
				rootObject = (AbstractMediaObject) value;
				break;
			}
		}
		return rootObject;
	}

	public T create(Transaction tx) {

		final AbstractMediaObject constObject = obtainConstructorObject();

		MediaPipeline pipeline = constObject.getInternalMediaPipeline();

		NonReadyRemoteObject remoteObject = new NonReadyRemoteObject(pipeline,
				NonReadyMode.TRANSACTION);

		T mediaObject = createMediaObjectConst(constObject, remoteObject, tx);

		MediaObjectCreationOperation op = new MediaObjectCreationOperation(
				clazz.getSimpleName(), props, mediaObject);

		tx.addOperation(op);

		return mediaObject;
	}

	public T build() {
		return create();
	}

	protected abstract T createMediaObject(RemoteObjectFacade remoteObject,
			Transaction tx);

}
