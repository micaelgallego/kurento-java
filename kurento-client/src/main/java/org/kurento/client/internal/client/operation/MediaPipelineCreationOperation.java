package org.kurento.client.internal.client.operation;

import java.util.ArrayList;
import java.util.List;

import org.kurento.client.MediaPipeline;
import org.kurento.client.internal.client.RemoteObject;
import org.kurento.client.internal.client.RomManager;

public class MediaPipelineCreationOperation extends Operation {

	private MediaPipeline mediaPipeline;
	private List<Operation> operations = new ArrayList<>();

	public MediaPipelineCreationOperation(MediaPipeline mediaPipeline) {
		this.mediaPipeline = mediaPipeline;
	}

	public void addOperation(Operation operation) {
		this.operations.add(operation);
	}

	@Override
	public void exec(RomManager manager) {
		RemoteObject remoteObject = manager.create("MediaPipeline");
		mediaPipeline.setRemoteObject(remoteObject);
		for (Operation operation : operations) {
			operation.exec(manager);
		}
	}
}
