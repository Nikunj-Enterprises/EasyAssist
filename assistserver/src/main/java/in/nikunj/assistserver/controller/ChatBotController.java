package in.nikunj.assistserver.controller;

import com.google.gson.Gson;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.ResourceUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import in.nikunj.assistserver.model.ChatMessage;


@RestController
@RequestMapping("/chatbot")
public class ChatBotController {
    private ConcurrentHashMap<String, Thread> botRegistry = new ConcurrentHashMap<>();
    @Autowired
    private MessageSendingOperations<String> messagingTemplate;

    @RequestMapping(value = "/connect/{userId}", method = RequestMethod.GET )
    public void connectToChatBot(@PathVariable("userId") String userId){
        if(botRegistry.containsKey(userId)) {

            botRegistry.get(userId).interrupt();
            botRegistry.remove(userId);

            System.out.println("botRegistry size is :"+botRegistry.size());
        }
        botRegistry.put(userId,
                new Thread() {
                    private boolean isInterrupted = false;
                    private Chat chatSession;

                    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

                    public ListenableFuture<StompSession> connect() {

                        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
                        List<Transport> transports = Collections.singletonList(webSocketTransport);

                        SockJsClient sockJsClient = new SockJsClient(transports);
                        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

                        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

                        String url = "ws://{host}:{port}/assistance-websocket";
                        return stompClient.connect(url, headers, new MyHandler(), "localhost", 8080);
                    }

                    public void subscribe(StompSession stompSession) throws ExecutionException, InterruptedException {
                        stompSession.subscribe("/topic/assistance/bot4"+userId, new StompFrameHandler() {

                            public Type getPayloadType(StompHeaders stompHeaders) {
                                return byte[].class;
                            }

                            public void handleFrame(StompHeaders stompHeaders, Object payload) {
                                //logger.info("Received greeting " + new String((byte[]) o));
                                System.out.println("Payloadreceived :"+new String((byte[])payload));
                                String payloadStr = new String((byte[])payload);
                                Gson gson = new Gson(); // Or use new GsonBuilder().create();
                                ChatMessage chatMessage = gson.fromJson(payloadStr, ChatMessage.class);
                                String message = chatSession.multisentenceRespond(chatMessage.getMessage().trim());

                                chatMessage.setMessage(message);
                                chatMessage.setRepliedTo(userId);
                                chatMessage.setRepliedBy("bot4"+userId);
                                messagingTemplate.convertAndSend("/topic/assistance/"+userId, gson.toJson(chatMessage));
                            }
                        });
                    }

                    class MyHandler extends StompSessionHandlerAdapter {
                        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
                            //logger.info("Now connected");
                            /*try {
                                subscribe(stompSession);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                            String message =
                                    JSONObject.stringToValue( "{\"helpSeekerId\":\""+userId+"\", \"acknowledgedBy\":\"bot4"+userId+"\"}").toString();
                            messagingTemplate.convertAndSend("/topic/assistance", message);
                        }
                    }

                    @Override
                    public void run() {
                        String resourcesPath = "C:/work/android_workspace/EasyAssist/assistserver/src/main/resources";

                        //MagicBooleans.trace_mode = TRACE_MODE;
                        Bot bot = new Bot("super", resourcesPath);
                        chatSession = new Chat(bot);
                        bot.brain.nodeStats();
                        StompSession stompSession = null;
                        long startTS = Calendar.getInstance().getTimeInMillis();
                        try {
                            stompSession = connect().get();
                            Thread.sleep(500);
                            subscribe(stompSession);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                        while (!isInterrupted
                                && ((Calendar.getInstance().getTimeInMillis() - startTS) < 300000L )){
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                isInterrupted = true;
                                stompSession.disconnect();
                            }
                        }
                    }
                }
        );
        botRegistry.get(userId).start();
    }

    @RequestMapping(value = "/disconnect/{userId}", method = RequestMethod.DELETE )
    public void disconnectChatBot(@PathVariable("userId") String userId){
        if(botRegistry.containsKey(userId)) {
            botRegistry.get(userId).interrupt();
            botRegistry.remove(userId);
        }
    }
}
