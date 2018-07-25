package in.nikunj.assistserver.controller;

import com.google.gson.Gson;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import in.nikunj.assistserver.model.HelpRequested;

@Controller
@RequestMapping("/chatbot")
public class ChatBotController {
    private static ConcurrentHashMap<String, Thread> botRegistry = new ConcurrentHashMap<>();
    @Autowired
    private MessageSendingOperations<String> messagingTemplate;

    @Value("${spring.chatbot.resource.path}")
    private String resourcesPath;

    @CrossOrigin
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
                        return stompClient.connect(url, headers, new MyHandler(), "127.0.0.1", 8090);
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
                                if(chatMessage.getRepliedBy().equals(userId) && chatMessage.getRepliedTo().equals("bot4"+userId)) {
                                    String message = chatSession.multisentenceRespond(chatMessage.getMessage().trim());
                                    message = preprocess(message);
                                    chatMessage.setMessage(message);
                                    chatMessage.setRepliedTo(userId);
                                    chatMessage.setRepliedBy("bot4" + userId);
                                    messagingTemplate.convertAndSend("/topic/assistance/" + userId, gson.toJson(chatMessage));
                                }
                            }
                        });
                    }
                    // breaks lines to fit chat bubble area
                    private String preprocess(String message){
                        if(message.equals("[command(cm)]")){
                            String formStr = "<style> form { display: block; margin-top: 0em; \n"+
                                    "padding: 10px 6px 5px 10px;margin:5px auto;}</style>\n"+
                                    "submit the form and we will get in touch:\n"+
                                    "<form>\n" +
                                    "  Full Name : <input type=\"text\" name=\"firstname\"><br>\n" +
                                    "  Contact no: <input type=\"text\" name=\"lastname\"><br>\n" +
                                    "  Email ID &nbsp; : <input type=\"text\" name=\"email\"><br>\n" +
                                    "  <input type=\"submit\" value=\"Submit\">\n" +
                                    "</form>";

                            return formStr;
                        }
                        StringBuilder builder = new StringBuilder();
                        int len = 0;
                        String[] words = message.split(" ");
                        for(String word : words){
                            len += word.length()+1;

                            if(word.contains("\n")){
                                len = 0;
                            }
                            if(len > 45){
                                builder.append('\n');
                                len = 0;
                            }
                            builder.append(word);
                            builder.append(" ");
                        }
                        return builder.toString();
                    }

                    class MyHandler extends StompSessionHandlerAdapter {
                        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
                            //logger.info("Now connected");
                            try {
                                Thread.sleep(500);
                                String message =
                                        JSONObject.stringToValue( "{\"helpSeekerId\":\""+userId+"\", \"acknowledgedBy\":\"bot4"+userId+"\"}").toString();
                                messagingTemplate.convertAndSend("/topic/assistance", message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void run() {
                        //String resourcesPath = "C:/work/android_workspace/EasyAssist/assistserver/src/main/resources";

                        MagicBooleans.trace_mode = false;
                        Bot bot = new Bot("kunskap", resourcesPath);
                        chatSession = new Chat(bot);
                        bot.brain.nodeStats();
                        StompSession stompSession = null;
                        long startTS = Calendar.getInstance().getTimeInMillis();
                        try {
                            stompSession = connect().get();
                            //Thread.sleep(500);
                            subscribe(stompSession);
                        }catch (Exception ex){
                            ex.printStackTrace();
                            isInterrupted = true;
                            stompSession.disconnect();
                            stompSession = null;
                            chatSession = null;
                        }

                        while (!isInterrupted
                                && ((Calendar.getInstance().getTimeInMillis() - startTS) < 300000L )){
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                isInterrupted = true;
                                stompSession.disconnect();
                                stompSession = null;
                                chatSession = null;
                            }
                        }
                    }
                }
        );
        botRegistry.get(userId).start();
    }

    @CrossOrigin
    @RequestMapping(value = "/disconnect/{userId}", method = RequestMethod.DELETE )
    public void disconnectChatBot(@PathVariable("userId") String userId){
        if(botRegistry.containsKey(userId)) {
            botRegistry.get(userId).interrupt();
            botRegistry.remove(userId);
        }
    }
}
