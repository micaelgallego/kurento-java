package org.kurento.client.internal.client.operation;

import org.kurento.client.AbstractMediaObject;
import org.kurento.client.internal.client.ListenerSubscriptionImpl;
import org.kurento.client.internal.client.RemoteObject.RemoteObjectEventListener;
import org.kurento.client.internal.client.RomManager;

public class SubscriptionOperation extends Operation {

	private AbstractMediaObject object;
	private String eventType;
	private RemoteObjectEventListener listener;
	private ListenerSubscriptionImpl listenerSubscription;

	public SubscriptionOperation(AbstractMediaObject object, String eventType,
			RemoteObjectEventListener listener) {
		this.object = object;
		this.eventType = eventType;
		this.listener = listener;
		this.listenerSubscription = new ListenerSubscriptionImpl(eventType,
				listener);
	}

	@Override
	public void exec(RomManager manager) {

		String subscription = object.getRemoteObject()
				.addEventListener(eventType, listener).getSubscription();

		listenerSubscription.setSubscription(subscription);
	}

	public ListenerSubscriptionImpl getListenerSubscription() {
		return listenerSubscription;
	}

}
