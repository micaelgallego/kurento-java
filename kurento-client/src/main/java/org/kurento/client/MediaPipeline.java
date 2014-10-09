/**
 * This file is generated with Kurento ktool-rom-processor.
 * Please don't edit. Changes should go to kms-interface-rom and
 * ktool-rom-processor templates.
 */
package org.kurento.client;

import org.kurento.client.internal.RemoteClass;
import org.kurento.client.internal.TransactionImpl;
import org.kurento.client.internal.client.NonReadyRemoteObject;
import org.kurento.client.internal.client.RemoteObjectFacade;
import org.kurento.client.internal.client.RomManager;

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

	// public MediaPipeline(RomManager manager) {
	// this(manager, new TransactionImpl(manager));
	// }

	// private MediaPipeline(RomManager manager, TransactionImpl tx) {
	// super(new NonReadyRemoteObject(tx.nextObjectRef(), null,
	// NonReadyMode.CREATION), tx);
	// this.setInternalMediaPipeline(this);
	// this.tx.addOperation(new MediaPipelineCreationOperation(this));
	// ((NonReadyRemoteObject) remoteObject).setPublicObject(this);
	// this.manager = manager;
	// }

	public MediaPipeline(RemoteObjectFacade remoteObject, Transaction tx) {
		super(remoteObject, tx);
		if (!(remoteObject instanceof NonReadyRemoteObject)) {
			manager = remoteObject.getRomManager();
		}
	}

	public static Builder with(KurentoClient client) {
		return new Builder(client);
	}

	synchronized void setRemoteObject(RemoteObjectFacade remoteObject) {
		super.setRemoteObject(remoteObject);
		if (!(remoteObject instanceof NonReadyRemoteObject)) {
			manager = remoteObject.getRomManager();
		}
	}

	// void addOperation(Operation operation) {
	// tx.addOperation(operation);
	// }

	public static class Builder extends AbstractBuilder<MediaPipeline> {

		public Builder(KurentoClient client) {
			super(MediaPipeline.class, client.getRomManager());
		}

		@Override
		protected MediaPipeline createMediaObject(
				RemoteObjectFacade remoteObject, Transaction tx) {
			return new MediaPipeline(remoteObject, tx);
		}
	}

	public Transaction beginTransaction() {
		return new TransactionImpl(manager);
	}

}
