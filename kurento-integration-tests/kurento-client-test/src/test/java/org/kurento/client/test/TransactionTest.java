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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kurento.client.HttpGetEndpoint;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.Transaction;
import org.kurento.client.TransactionNotExecutedException;
import org.kurento.client.test.util.AsyncResultManager;
import org.kurento.test.base.KurentoClientTest;

public class TransactionTest extends KurentoClientTest {

	@Test
	public void transactionTest() throws InterruptedException,
			ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();

		player.connect(httpGetEndpoint);

		pipeline.start();
		// End pipeline creation

		// Atomic operation
		String url = httpGetEndpoint.getUrl();
		// End atomic operation

		// Explicit transaction
		Transaction tx = pipeline.newTransaction();
		player.play(tx);
		Future<String> fUrl = httpGetEndpoint.getUrl(tx);
		pipeline.release(tx);
		tx.exec();
		// End explicit transaction

		assertThat(url, is(fUrl.get()));
	}

	@Test
	public void transactionCreation() throws InterruptedException,
			ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		pipeline.start();

		// Atomic creation
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		// End atomic operation

		// Creation in explicit transaction
		Transaction tx = pipeline.newTransaction();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create(tx);
		player.connect(httpGetEndpoint, tx);
		tx.exec();
		// End transaction

		String url = httpGetEndpoint.getUrl();

		assertThat(url, not(nullValue()));
	}

	@Test(expected = TransactionNotExecutedException.class)
	public void usePlainMethodsInNewObjectsInsideTx()
			throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		pipeline.start();

		// Creation in explicit transaction
		Transaction tx = pipeline.newTransaction();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create(tx);

		// TransactionNotExecutedExcetion should be thrown
		httpGetEndpoint.connect(player);

	}

	@Test(expected = TransactionNotExecutedException.class)
	public void usePlainMethodsWithNewObjectsAsParamsInsideTx()
			throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		pipeline.start();

		// Creation in explicit transaction
		Transaction tx = pipeline.newTransaction();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create(tx);

		// TransactionNotExecutedExcetion should be thrown
		player.connect(httpGetEndpoint);

	}

	@Test
	public void useCreationTransactionTest() throws InterruptedException,
			ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);
		Future<String> url = httpGetEndpoint.getUrl(pipeline
				.getCreationTransaction());
		pipeline.start();

		assertThat(url.get(), is(notNullValue()));
	}

	@Test
	public void useWhenReadyMethodsTest() throws InterruptedException,
			ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);
		Future<String> url = httpGetEndpoint.getUrlWhenReady();

		pipeline.start();

		assertThat(url.get(), is(notNullValue()));
	}

	@Test
	public void isReadyTest() throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);

		assertThat(player.isReady(), is(false));

		pipeline.start();

		assertThat(player.isReady(), is(true));
	}

	@Test
	public void pipelineStartingAsyncTest() throws InterruptedException,
			ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);

		AsyncResultManager<MediaPipeline> async = new AsyncResultManager<>(
				"async start");

		pipeline.start(async.getContinuation());

		assertThat(async.waitForResult(), is(pipeline));
	}

	@Test
	public void waitReadyTest() throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		final PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);

		final CountDownLatch readyLatch = new CountDownLatch(1);

		new Thread() {
			public void run() {
				try {
					player.waitReady();
					readyLatch.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

		assertThat(readyLatch.getCount(), is(1l));

		pipeline.start();

		if (!readyLatch.await(5000, TimeUnit.SECONDS)) {
			fail("waitForReady not unblocked in 5s");
		}
	}

	@Test
	public void whenReadyTest() throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();
		final PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();
		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();
		player.connect(httpGetEndpoint);

		AsyncResultManager<PlayerEndpoint> async = new AsyncResultManager<>(
				"whenReady");

		player.whenReady(async.getContinuation());

		pipeline.start();

		PlayerEndpoint newPlayer = async.waitForResult();

		assertThat(player, is(newPlayer));
	}

	@Test
	public void futureTest() throws InterruptedException, ExecutionException {

		// Pipeline creation (implicit transaction)
		MediaPipeline pipeline = MediaPipeline.with(kurentoClient).create();

		PlayerEndpoint player = PlayerEndpoint.with(pipeline,
				"http://files.kurento.org/video/small.webm").create();

		HttpGetEndpoint httpGetEndpoint = HttpGetEndpoint.with(pipeline)
				.create();

		player.connect(httpGetEndpoint);

		pipeline.start();
		// End pipeline creation

		// Atomic operation
		String url = httpGetEndpoint.getUrl();
		MediaPipeline rPipeline = httpGetEndpoint.getMediaPipeline();
		String uri = player.getUri();
		// End atomic operation

		// Explicit transaction
		Transaction tx = pipeline.newTransaction();
		Future<String> fUrl = httpGetEndpoint.getUrl(tx);
		Future<MediaPipeline> fRPipeline = httpGetEndpoint.getMediaPipeline(tx);
		Future<String> fUri = player.getUri(tx);
		tx.exec();
		// End explicit transaction

		assertThat(url, is(fUrl.get()));
		assertThat(uri, is(fUri.get()));

		MediaPipeline fRPipelineGet = fRPipeline.get();

		System.out.println(rPipeline);
		System.out.println(fRPipelineGet);

		assertThat(rPipeline, is(fRPipelineGet));
	}
}
