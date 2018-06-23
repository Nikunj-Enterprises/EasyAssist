package com.easyapper.grpublisher.service;

import com.easyapper.grpublisher.service.listener.MessageActionListener;
import com.easyapper.grpublisher.service.listener.RequesterMessageHandler;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class MessagingService implements IMqttActionListener{
    protected MqttAsyncClient client;
    protected MemoryPersistence memoryPersistence;
    protected IMqttToken connectToken;
    protected IMqttToken broadcastSubcrpnToken;
    protected IMqttToken p2pSubcrpnToken;

    private static final String MESSAGING_BROKER_URI = "tcp://localhost:1883";
    private static final String clientID = "client";

    public MessagingService(){
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            // options.setUserName(
            //	"replace with your username");
            // options.setPassword(
            //    "replace with your password"
            //    .toCharArray());
            // Replace with ssl:// and work with TLS/SSL
            // best practices in a
            // production environment
            memoryPersistence = new MemoryPersistence();
            String serverURI = MESSAGING_BROKER_URI;
            client = new MqttAsyncClient(serverURI, clientID, memoryPersistence);

            client.setCallback(new RequesterMessageHandler());
            connectToken = client.connect(options,null,this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MessageActionListener publishMessage(String appId, String destination, String message){
        byte[] bytesMessage;
        String topic = "/easy/"+appId+"/"+destination;

        try {
            bytesMessage =
                    message.getBytes("UTF-8");
            MqttMessage messageObj;
            messageObj = new MqttMessage(bytesMessage);
            Object userContext = "dummyContext";
            MessageActionListener actionListener =
                    new MessageActionListener(topic, message, userContext);
            client.publish(topic, messageObj, userContext,	actionListener);
            return actionListener;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (MqttException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
         System.out.println("MessagingService onSuccess:"+asyncActionToken);
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        System.out.println("MessagingService onFailure:"+asyncActionToken);
    }

    public boolean isConnected() {
        return (client != null) && (client.isConnected());
    }

    public void disconnect() throws MqttException {
        if(isConnected()) {
            this.client.disconnect();
        }
    }
}
