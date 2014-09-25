package org.kurento.client.internal.client.operation;

import java.lang.reflect.Type;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.internal.client.RomManager;
import org.kurento.jsonrpc.Props;

public class InvokeOperation extends Operation {

	private AbstractMediaObject object;
	private String method;
	private Props params;
	private Type returnType;

	public InvokeOperation(AbstractMediaObject object, String method,
			Props params, Type returnType) {
		super();
		this.object = object;
		this.method = method;
		this.params = params;
		this.returnType = returnType;
	}

	@Override
	public void exec(RomManager manager) {
		manager.invoke(object.getRemoteObject().getObjectRef(), method, params,
				returnType);
	}

}
