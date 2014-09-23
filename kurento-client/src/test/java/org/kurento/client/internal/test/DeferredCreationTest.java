package org.kurento.client.internal.test;

import org.junit.Assert;
import org.junit.Test;
import org.kurento.client.HttpGetEndpoint;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;

public class DeferredCreationTest {

	@Test
	public void test() {

		KurentoClient client = KurentoClient
				.create("ws://localhost:8888/kurento");

		MediaPipeline pipeline = MediaPipeline.with(client).create();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();

		player.connect(httpGetEndpoint);

		String url = httpGetEndpoint.getUrl();

		player.release();

		Assert.assertNotSame("The URL shouldn't be empty", "", url);

	}

}
