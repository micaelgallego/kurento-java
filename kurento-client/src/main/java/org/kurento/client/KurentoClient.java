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
package org.kurento.client;

import javax.annotation.PreDestroy;

import org.kurento.client.internal.client.RomManager;
import org.kurento.client.internal.transport.jsonrpc.RomClientJsonRpcClient;
import org.kurento.jsonrpc.client.JsonRpcClient;
import org.kurento.jsonrpc.client.JsonRpcClientWebSocket;

/**
 * Factory to create {@link MediaPipeline} in the media server.
 *
 * @author Luis López (llopez@gsyc.es)
 * @author Ivan Gracia (igracia@gsyc.es)
 * @since 2.0.0
 */
public class KurentoClient {

	protected RomManager manager;

	public static KurentoClient create(String websocketUrl) {
		return new KurentoClient(new JsonRpcClientWebSocket(websocketUrl));
	}

	public static KurentoClient create(String websocketUrl,
			KurentoConnectionListener listener) {
		return new KurentoClient(new JsonRpcClientWebSocket(websocketUrl,
				JsonRpcConnectionListenerKurento.create(listener)));
	}

	@PreDestroy
	public void destroy() {
		manager.destroy();
	}

	KurentoClient(JsonRpcClient client) {
		this.manager = new RomManager(new RomClientJsonRpcClient(client));
	}

	KurentoClient(RomManager manager) {
		this.manager = manager;
	}

	RomManager getRomManager() {
		return manager;
	}

	public static KurentoClient createFromJsonRpcClient(
			JsonRpcClient jsonRpcClient) {
		return new KurentoClient(jsonRpcClient);
	}
}
