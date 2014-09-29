package org.kurento.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.kurento.client.internal.client.RomManager;
import org.kurento.client.internal.client.operation.Operation;

public class TransactionImpl implements Transaction {

	private List<Operation> operations = new ArrayList<>();
	private RomManager manager;

	public TransactionImpl(RomManager manager) {
		this.manager = manager;
	}

	@SuppressWarnings("unchecked")
	public <E> Future<E> addOperation(Operation op) {
		this.operations.add(op);
		return (Future<E>) op.getFuture();
	}

	public void exec() {
		manager.execOperations(operations);
	}

	public void exec(Continuation<Void> continuation) {
		manager.execOperations(operations, continuation);
	}
}
