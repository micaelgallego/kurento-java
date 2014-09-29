/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.kurento.client.test;

import org.junit.Assert;
import org.junit.Test;
import org.kurento.client.HttpGetEndpoint;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaPipelineNotStartedException;
import org.kurento.client.PlayerEndpoint;
import org.kurento.test.base.KurentoClientTest;

public class MediaPipelineCreationTest extends KurentoClientTest {

	@Test
	public void normalUseTest() {

		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();

		player.connect(httpGetEndpoint);

		pipeline.start();

		String url = httpGetEndpoint.getUrl();

		player.release();

		Assert.assertNotSame("The URL shouldn't be empty", "", url);

		pipeline.release();
	}

	@Test
	public void earlyPipelineCreationTest() {

		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		pipeline.start();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();

		player.connect(httpGetEndpoint);

		String url = httpGetEndpoint.getUrl();

		player.release();

		Assert.assertNotSame("The URL shouldn't be empty", "", url);

		pipeline.release();
	}

	@Test(expected = MediaPipelineNotStartedException.class)
	public void nonStartedMediaPipelineTest() {

		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		player.play();
	}

	@Test(expected = IllegalStateException.class)
	public void startedTwiceMediaPipelineTest() {

		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		pipeline.start();
		pipeline.start();
	}
}
