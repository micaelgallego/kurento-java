package org.kurento.client.internal.test;


public class DeferredCreationTest2 {

	// private static RemoteObjectFactory factory;
	//
	// @BeforeClass
	// public static void initFactory() {
	// factory = new RemoteObjectFactory(
	// new RomClientJsonRpcClient(
	// new JsonRpcClientLocal(
	// new RomServerJsonRpcHandler(
	// "org.kurento.client.internal.test.model.server",
	// "Impl"))));
	// }
	//
	// @Test
	// public void objectRefTest() {
	//
	// RemoteObjectConnected remoteObject = factory.create("SampleClass",
	// new Props().add("att1", "AAA").add("att2", false));
	//
	// SampleClass obj3 = obj.echoObjectRef(obj2);
	//
	// assertEquals(obj3.getAtt1(), obj2.getAtt1());
	// assertEquals(obj3.getAtt2(), obj2.getAtt2());
	// }
	//
	// @Test
	// public void objectRefTestAsync() throws InterruptedException {
	//
	// SampleClass obj = SampleClass.with("AAA", false, factory)
	// .withAtt3(0.5f).withAtt4(22).create();
	//
	// final SampleClass obj2 = SampleClass.with("BBB", false, factory)
	// .withAtt3(0.5f).withAtt4(22).create();
	//
	// final BlockingQueue<SampleClass> queue = new ArrayBlockingQueue<>(1);
	//
	// obj.echoObjectRef(obj2, new Continuation<SampleClass>() {
	//
	// @Override
	// public void onSuccess(SampleClass obj3) {
	// queue.add(obj3);
	// }
	//
	// @Override
	// public void onError(Throwable cause) {
	//
	// }
	// });
	//
	// SampleClass obj3 = queue.poll(500, MILLISECONDS);
	//
	// Assert.assertNotNull(obj3);
	//
	// assertEquals(obj3.getAtt1(), obj2.getAtt1());
	// assertEquals(obj3.getAtt2(), obj2.getAtt2());
	// }

}
