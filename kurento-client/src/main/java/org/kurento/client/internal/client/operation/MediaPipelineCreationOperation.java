package org.kurento.client.internal.client.operation;

import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.client.internal.client.DefaultContinuation;
import org.kurento.client.internal.client.RemoteObject;
import org.kurento.client.internal.client.RemoteObjectFacade;
import org.kurento.client.internal.client.RomManager;

public class MediaPipelineCreationOperation extends Operation {

	private MediaPipeline mediaPipeline;

	public MediaPipelineCreationOperation(MediaPipeline mediaPipeline) {
		this.mediaPipeline = mediaPipeline;
	}

	@Override
	public void exec(RomManager manager) {
		RemoteObject remoteObject = manager.create("MediaPipeline");
		mediaPipeline.setRemoteObject(remoteObject);
	}

	@Override
	public void exec(final RomManager manager, final Continuation<Void> cont) {
		manager.create("MediaPipeline",
				new DefaultContinuation<RemoteObjectFacade>(cont) {
					@Override
					public void onSuccess(RemoteObjectFacade remoteObject)
							throws Exception {
						mediaPipeline.setRemoteObject(remoteObject);
						cont.onSuccess(null);
					}
				});

	}
}
