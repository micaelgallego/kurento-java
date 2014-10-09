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

import static org.kurento.client.test.RtpEndpoint2Test.URL_POINTER_DETECTOR;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kurento.client.EndOfStreamEvent;
import org.kurento.client.FaceOverlayFilter;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.test.util.AsyncEventManager;
import org.kurento.client.test.util.AsyncResultManager;
import org.kurento.client.test.util.MediaPipelineAsyncBaseTest;

/**
 * {@link FaceOverlayFilter} test suite.
 *
 * @author Ivan Gracia (igracia@gsyc.es)
 * @since 2.0.1
 *
 */
public class FaceOverlayFilterAsyncTest extends MediaPipelineAsyncBaseTest {

	private PlayerEndpoint player;

	private FaceOverlayFilter overlayFilter;

	@Before
	public void setupMediaElements() throws InterruptedException {

		player = PlayerEndpoint.with(pipeline, URL_POINTER_DETECTOR).create();

		AsyncResultManager<FaceOverlayFilter> async = new AsyncResultManager<>(
				"FaceOverlayFilter creation");

		FaceOverlayFilter.with(pipeline).createAsync(async.getContinuation());

		overlayFilter = async.waitForResult();
	}

	@After
	public void teardownMediaElements() throws InterruptedException {
		player.release();
		releaseMediaObject(overlayFilter);
	}

	/**
	 * Test if a {@link FaceOverlayFilter} can be created in the KMS. The filter
	 * is pipelined with a {@link PlayerEndpoint}, which feeds video to the
	 * filter. This test depends on the correct behaviour of the player and its
	 * events.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testFaceOverlayFilter() throws InterruptedException {

		player.connect(overlayFilter);

		AsyncEventManager<EndOfStreamEvent> async = new AsyncEventManager<>(
				"EndOfStream event");

		pipeline.start();

		player.addEndOfStreamListener(async.getMediaEventListener());

		player.play();

		async.waitForResult();
	}

}
