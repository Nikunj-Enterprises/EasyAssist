package in.nikunj.assistserver.controller;

import in.nikunj.assistserver.model.ChatMessage;
import in.nikunj.assistserver.model.HelpRequested;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Controller;

import java.util.concurrent.atomic.AtomicBoolean;


@Controller
public class AssistanceController implements ApplicationListener<BrokerAvailabilityEvent> {

    @Autowired
    private MessageSendingOperations<String> messagingTemplate;

    private AtomicBoolean brokerAvailable = new AtomicBoolean();

    @MessageMapping("/requested/{helpSeekerId}")
    @SendTo("/topic/assistance")
    public HelpRequested askForAssistance(@DestinationVariable String helpSeekerId, String message){
        HelpRequested requestMsg = new HelpRequested();
        requestMsg.setHelpSeekerId(helpSeekerId);
        return requestMsg;
    }

    @MessageMapping("/{helpSeekerId}/acknowledged/{helpProviderId}")
    @SendTo("/topic/assistance")
    public HelpRequested acknowledgeRequest(@DestinationVariable String helpSeekerId, @DestinationVariable String helpProviderId){
        HelpRequested ackMsg = new HelpRequested();
        ackMsg.setHelpSeekerId(helpSeekerId);
        ackMsg.setAcknowledgedBy(helpProviderId);
        return ackMsg;
    }

    @MessageMapping("/{repliedBy}/chat/{repliedTo}")
    public void chat(@DestinationVariable String repliedBy, @DestinationVariable String repliedTo, String message){
        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setRepliedBy(repliedBy);
        chatMsg.setRepliedTo(repliedTo);
        chatMsg.setMessage(message);

        // map (customerId => chatSession Obj)
        messagingTemplate.convertAndSend("/topic/assistance/"+repliedTo,chatMsg);
        //return  chatMsg;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    @Override
    public void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable());
    }
}
