package com.easyapper.grpublisher.service.listener;

import 
org.eclipse.paho.client.mqttv3.IMqttActionListener;
import 
org.eclipse.paho.client.mqttv3.IMqttToken;

public class MessageActionListener implements IMqttActionListener {
  protected final String messageText;
  protected final String topic;
  protected final Object userContext;

  public MessageActionListener(
    String topic, 
    String messageText,
    Object userContext) {
      this.topic = topic;
      this.messageText = messageText;
      this.userContext = userContext;
  }

  @Override
  public void onSuccess(
    IMqttToken asyncActionToken) {
      if ((asyncActionToken != null) && 
           asyncActionToken.getUserContext()
             .equals(userContext)) 
      {
       System.out.println( String.format(
         "Message '%s' published to topic '%s'",
         messageText, topic));
      }
    }

  @Override
  public void onFailure(
      IMqttToken asyncActionToken, 
      Throwable exception) {
          exception.printStackTrace();
	 }
}
