package org.kurento.client.internal.test;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kurento.client.internal.client.RemoteObjectFactory;
import org.kurento.client.internal.test.model.Sample2;
import org.kurento.client.internal.transport.jsonrpc.RomClientJsonRpcClient;
import org.kurento.client.internal.transport.jsonrpc.RomServerJsonRpcHandler;
import org.kurento.jsonrpc.client.JsonRpcClientLocal;

public class ConstructionTest {

	private static RemoteObjectFactory factory2;

	@BeforeClass
	public static void initFactory() {
		factory2 = new RemoteObjectFactory(new RomClientJsonRpcClient(
				new JsonRpcClientLocal(new RomServerJsonRpcHandler(
						"org.kurento.client.internal.test.model", "Impl"))));
	}

	@Test
	public void initObject() {

		Sample2 obj = new Sample2.Builder("XXX", 33, factory2).withAtt3(0.5f)
				.att4().create();

		String att1 = obj.getAtt1();
		int att2 = obj.getAtt2();
		float att3 = obj.getAtt3();
		boolean att4 = obj.getAtt4();

		assertEquals(att1, "XXX");
		assertEquals(att2, 33);
		assertEquals(att3, 0.5f, 0.01);
		assertEquals(att4, true);

	}

}
