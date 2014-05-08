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
package com.kurento.kmf.thrift.pool;

import com.kurento.kmf.common.exception.KurentoException;

/**
 * This exception is thrown when acquiring or returning clients to/from the
 * pool.
 * 
 * @author Ivan Gracia (izanmail@gmail.com)
 *
 */
public class ClientPoolException extends KurentoException {

	private static final long serialVersionUID = -6427664638336803375L;

	public ClientPoolException(String message) {
		super(message);
	}

	public ClientPoolException(String message, Throwable cause) {
		super(message, cause);
	}
}