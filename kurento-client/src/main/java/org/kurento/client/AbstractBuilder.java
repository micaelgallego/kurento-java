/**
 * This file is generated with Kurento ktool-rom-processor.
 * Please don't edit. Changes shoult go to kms-interface-rom and
 * ktool-rom-processor templates.
 */
package org.kurento.client;

import java.lang.reflect.InvocationTargetException;

import org.kurento.client.internal.client.DefaultContinuation;
import org.kurento.client.internal.client.RemoteObject;
import org.kurento.client.internal.client.RemoteObjectFacade;
import org.kurento.client.internal.client.RomManager;
import org.kurento.commons.exception.KurentoException;
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
public class AbstractBuilder<T> {

	protected final Props props;
	private final RomManager manager;
	private final Class<?> clazz;

	public AbstractBuilder(Class<?> clazz, MediaObject mediaObject) {

		this.props = new Props();
		this.clazz = clazz;
		this.manager = ((AbstractMediaObject) mediaObject).getRomManager();
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

		RemoteObjectFacade remoteObject = manager.create(clazz.getSimpleName(),
				props);

		return createMediaObject(remoteObject);
	}

	@SuppressWarnings("unchecked")
	private T createMediaObject(RemoteObjectFacade remoteObject) {
		try {

			return (T) clazz.getConstructor(RemoteObject.class).newInstance(
					remoteObject);

		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new KurentoException(
					"Execption creating media object of type '"
							+ clazz.getName() + "'", e);
		}
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
	public void createAsync(final Continuation<T> continuation) {

		manager.create(clazz.getSimpleName(), props,
				new DefaultContinuation<RemoteObject>(continuation) {
					@Override
					public void onSuccess(RemoteObject remoteObject) {
						try {
							continuation
									.onSuccess(createMediaObject(remoteObject));
						} catch (Exception e) {
							log.warn(
									"[Continuation] error invoking onSuccess implemented by client",
									e);
						}
					}
				});

	}

}
