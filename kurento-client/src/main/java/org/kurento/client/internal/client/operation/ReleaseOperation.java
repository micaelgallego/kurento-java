package org.kurento.client.internal.client.operation;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RomManager;

public class ReleaseOperation extends Operation {

	private AbstractMediaObject mediaObject;

	public ReleaseOperation(AbstractMediaObject mediaObject) {
		this.mediaObject = mediaObject;
	}

	@Override
	public void exec(RomManager manager) {
		manager.release(mediaObject.getRemoteObject().getObjectRef());
	}

	@Override
	public void exec(RomManager manager, final Continuation<Void> cont) {
		manager.release(mediaObject.getRemoteObject().getObjectRef(), cont);
	}

}
