package org.kurento.client;

import java.util.concurrent.Future;

import org.kurento.client.internal.client.operation.Operation;

public interface Transaction {

	public void exec();

	public <E> Future<E> addOperation(Operation op);

}
