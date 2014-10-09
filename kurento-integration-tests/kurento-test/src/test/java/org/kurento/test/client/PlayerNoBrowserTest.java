/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
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
package org.kurento.test.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kurento.client.HttpGetEndpoint;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.commons.testing.SystemKurentoClientTests;
import org.kurento.test.base.KurentoClientTest;

/**
 * <strong>Description</strong>: HTTP Player, tested with HttpClient (not
 * Selenium).<br/>
 * <strong>Pipeline</strong>:
 * <ul>
 * <li>PlayerEndpoint -> HttpGetEndpoint</li>
 * </ul>
 * <strong>Pass criteria</strong>:
 * <ul>
 * <li>Received content/type is video/webm</li>
 * </ul>
 * 
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 4.2.3
 */
@Category(SystemKurentoClientTests.class)
public class PlayerNoBrowserTest extends KurentoClientTest {

	@Test
	public void testPlayer() throws Exception {
		// Media Pipeline
		MediaPipeline mp = kurentoClient.createMediaPipeline();
		PlayerEndpoint playerEP = new PlayerEndpoint.Builder(mp,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpEP = new HttpGetEndpoint.Builder(mp)
				.terminateOnEOS().create();
		playerEP.connect(httpEP);
		playerEP.play();

		// Test execution
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(httpEP.getUrl());
		HttpResponse response = client.execute(httpGet);
		HttpEntity resEntity = response.getEntity();

		// Assertions
		Assert.assertEquals("Response content-type must be video/webm",
				"video/webm", resEntity.getContentType().getValue());

		// Release Media Pipeline
		mp.release();
	}

}
