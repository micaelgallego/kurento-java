/**
 * This file is generated with Kurento ktool-rom-processor.
 * Please don't edit. Changes should go to kms-interface-rom and
 * ktool-rom-processor templates.
 */
package org.kurento.client;

import org.kurento.client.internal.RemoteClass;
import org.kurento.client.internal.TransactionImpl;
import org.kurento.client.internal.client.NonReadyRemoteObject;
import org.kurento.client.internal.client.NonReadyRemoteObject.NonReadyMode;
import org.kurento.client.internal.client.RomManager;
import org.kurento.client.internal.client.operation.MediaPipelineCreationOperation;
import org.kurento.client.internal.client.operation.Operation;

/**
 *
 * A pipeline is a container for a collection of {@link module
 * :core/abstracts.MediaElement MediaElements} and
 * :rom:cls:`MediaMixers<MediaMixer>`. It offers the methods needed to control
 * the creation and connection of elements inside a certain pipeline. *
 **/
@RemoteClass
public class MediaPipeline extends MediaObject {

	private RomManager manager;

	public MediaPipeline(RomManager manager) {
		this(manager, new TransactionImpl(manager));
	}

	private MediaPipeline(RomManager manager, TransactionImpl tx) {
		super(new NonReadyRemoteObject(tx.nextObjectRef(), null,
				NonReadyMode.CREATION), tx);
		this.setInternalMediaPipeline(this);
		this.tx.addOperation(new MediaPipelineCreationOperation(this));
		((NonReadyRemoteObject) remoteObject).setPublicObject(this);
		this.manager = manager;
	}

	public static Builder with(KurentoClient client) {
		return new Builder(client);
	}

	public void start() {
		if (isReady()) {
			throw new IllegalStateException("MediaPipeline is yet started");
		}
		tx.exec();
	}

	public void start(final Continuation<MediaPipeline> continuation) {
		if (isReady()) {
			throw new IllegalStateException("MediaPipeline is yet started");
		}
		tx.exec(new Continuation<Void>() {
			@Override
			public void onSuccess(Void result) throws Exception {
				continuation.onSuccess(MediaPipeline.this);
			}

			@Override
			public void onError(Throwable cause) throws Exception {
				continuation.onError(cause);
			}
		});
	}

	void addOperation(Operation operation) {
		tx.addOperation(operation);
	}

	public Transaction getCreationTransaction() {
		return tx;
	}

	public static class Builder {

		KurentoClient client;

		Builder(KurentoClient client) {
			this.client = client;
		}

		public MediaPipeline create() {
			return new MediaPipeline(client.getRomManager());
		}

		public void createAsync(final Continuation<MediaPipeline> continuation) {
			try {
				continuation.onSuccess(create());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Transaction newTransaction() {
		return new TransactionImpl(manager);
	}

}
