package org.kurento.client;

import org.kurento.client.internal.client.RemoteObjectFacade;
import org.kurento.client.internal.client.operation.Operation;

public class InternalInfoGetter {

	public static RemoteObjectFacade getRemoteObject(
			AbstractMediaObject mediaObject) {
		return mediaObject.getRemoteObject();
	}

	public static MediaPipeline getInternalMediaPipeline(
			AbstractMediaObject mediaObject) {
		return mediaObject.getInternalMediaPipeline();
	}

	public static void addOperation(MediaPipeline mediaPipeline,
			Operation operation) {
		mediaPipeline.addOperation(operation);
	}

	public static void setRemoteObject(AbstractMediaObject mediaObject,
			RemoteObjectFacade remoteObject) {
		mediaObject.setRemoteObject(remoteObject);
	}
}
