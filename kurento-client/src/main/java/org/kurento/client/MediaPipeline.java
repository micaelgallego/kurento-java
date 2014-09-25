/**
 * This file is generated with Kurento ktool-rom-processor.
 * Please don't edit. Changes should go to kms-interface-rom and
 * ktool-rom-processor templates.
 */
package org.kurento.client;

import org.kurento.client.internal.RemoteClass;
import org.kurento.client.internal.client.NonReadyRemoteObject;
import org.kurento.client.internal.client.RemoteObjectFacade;
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

	private MediaPipelineCreationOperation pipelineCreationOp = new MediaPipelineCreationOperation(
			this);
	private RomManager manager;

	public MediaPipeline(RemoteObjectFacade remoteObject) {
		super(remoteObject);
		this.setInternalMediaPipeline(this);
	}

	public MediaPipeline(RomManager manager) {
		this(new NonReadyRemoteObject());
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
		pipelineCreationOp.exec(manager);
	}

	public void addOperation(Operation operation) {
		pipelineCreationOp.addOperation(operation);
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

}
