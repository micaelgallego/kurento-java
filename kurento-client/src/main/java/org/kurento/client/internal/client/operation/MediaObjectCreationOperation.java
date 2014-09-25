package org.kurento.client.internal.client.operation;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.internal.client.RemoteObject;
import org.kurento.client.internal.client.RomManager;
import org.kurento.jsonrpc.Props;

public class MediaObjectCreationOperation extends Operation {

	public String className;
	public Props constructorParams;
	public AbstractMediaObject mediaObject;

	public MediaObjectCreationOperation(String className,
			Props constructorParams, AbstractMediaObject mediaObject) {
		this.className = className;
		this.constructorParams = constructorParams;
		this.mediaObject = mediaObject;
	}

	public void exec(RomManager manager) {
		RemoteObject remoteObject = manager
				.create(className, constructorParams);

		mediaObject.setRemoteObject(remoteObject);
	}
}
