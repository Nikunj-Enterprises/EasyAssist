package com.easyapper.grpublisher.service.listener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RequesterMessageHandler implements MqttCallback{

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("Connection is lost");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		String messageText = new String(message.getPayload(), "UTF-8");
		System.out.println(String.format("Requester received msg:%s, at topic:%s", messageText, topic));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		System.out.println("Message delivered");
	}

}
