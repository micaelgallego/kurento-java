package org.kurento.client.internal.client.operation;

import java.util.concurrent.Future;

import org.kurento.client.Continuation;
import org.kurento.client.internal.client.RomManager;

import com.google.common.util.concurrent.SettableFuture;

public abstract class Operation {

	protected SettableFuture<Object> future = SettableFuture.create();

	public abstract void exec(RomManager manager);

	public abstract void exec(RomManager manager, Continuation<Void> cont);

	public Future<Object> getFuture() {
		return future;
	}

}
