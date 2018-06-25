package com.easyapper.bloodline;

import android.annotation.SuppressLint;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.easyapper.bloodline.BloodGroup.O2;

public class ProcessManager {
    private List<Peer> donorList = new ArrayList<>();
    private List<Message> reqMsgList = new ArrayList<>();
    private StateChangeNotifier stateChangeNotifier;
    private LocationReader locationReader;
    private User user;
    private ConnHandler authHandler;
    private boolean userRaisedReq = false;

    public synchronized boolean isUserRaisedReq() {
        return userRaisedReq;
    }

    public synchronized void setUserRaisedReq(boolean userRaisedReq) {
        this.userRaisedReq = userRaisedReq;
    }

    public synchronized List<Peer> getDonorList() {
        return donorList;
    }

    public synchronized List<Message> getReqMsgList() {
        return reqMsgList;
    }

    public void connect(User userObj, ConnHandler handler){
        if( this.locationReader == null ||  this.stateChangeNotifier == null){
            throw new RuntimeException("call-back handlers not defined");
        }
        this.user = userObj;
        this.authHandler = handler;

        new Thread() {
            @SuppressLint("NewApi")
            public void run() {
                int count = 0;
                while (count < 4) {
                    count++;
                    Message message = new Message();
                    message.setBroadcast(true);
                    message.setSentBy("MrX" + count);
                    message.setSentTo(O2.getValue());
                    message.setCreatedAt(Date.from(Instant.now()).getTime());
                    message.setLang(locationReader.getLangitude());
                    message.setLat(locationReader.getLatitude());
                    long msgId = (Math.round(Date.from(Instant.now()).getTime() -count) / 10000);
                    message.setMsgId(msgId);
                    message.setContent("Need O-ve blood for young thalassemia patient");
                    synchronized (reqMsgList) {
                        reqMsgList.add(message);
                        stateChangeNotifier.onProcessMgrListChange("message");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                synchronized (this) {
                    while (!userRaisedReq) {
                        try {
                            wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Peer peer = new Peer();
                peer.setPeerId("DonorX");
                peer.setLang(28.6292f);
                peer.setLat(77.1371f);
                synchronized (donorList){
                    donorList.add(peer);
                }
                stateChangeNotifier.onProcessMgrListChange("peer");
            }
        }.start();

        handler.OnSuccess(200, "ABCToken.XYZ.1234");
    }

    public synchronized void setStateChangeNotifier(StateChangeNotifier stateChangeNotifier){
        this.stateChangeNotifier = stateChangeNotifier;
    }

    public synchronized void setLocationReader(LocationReader locnReader){
        this.locationReader = locnReader;
    }

    public synchronized void broadcastReq(Message msg, ConnHandler handler){
        msg.setSentBy(user.getUserId());
        msg.setLat(locationReader.getLatitude());
        msg.setLang(locationReader.getLangitude());
        msg.setBroadcast(true);
        setUserRaisedReq(true);
        // notify that user has raised req
        notifyAll();
        handler.OnSuccess(200, "ABCToken.XYZ.1234");
        // Let donor send some message
        new Thread() {
            public void run() {
                synchronized (this) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    getDonorList().get(0).addMessage("This new message from Donor");
                }
                stateChangeNotifier.onPeerMsgListChange(getDonorList().get(0).getPeerId());
            }
        }.start();
    }

    public void cancelRequest(ConnHandler handler){
        handler.OnSuccess(200, "ABCToken.XYZ.1234");
    }

    public synchronized void sendMsgToPeer(Message msg, ConnHandler handler ){
        Peer peer = new Peer(msg.getSentTo());
        if(this.donorList.contains(peer)){
            int idx = this.donorList.indexOf(peer);
            this.donorList.get(idx).addMessage("Hello There !!");
            this.stateChangeNotifier.onPeerMsgListChange(msg.getSentTo());
        }
        handler.OnSuccess(200, "ABCToken.XYZ.1234");
    }

    public void acknowledgeReq(long reqMsgId, ConnHandler handler){
        handler.OnSuccess(200, "ABCToken.XYZ.1234");
    }

    public synchronized void removeMsgOfList(long reqMsgId) {
        Message msgOne = null;
        for (Message msg : reqMsgList) {
            if (msg.getMsgId() == reqMsgId) {
                msgOne = msg;
                break;
            }
        }
        if (msgOne != null) {
            this.reqMsgList.remove(msgOne);
            this.stateChangeNotifier.onProcessMgrListChange("message");
        }
    }

    public List<Message> getChatMessageList(String peerId){
        Message msg = new Message();
        msg.setSentBy(user.getUserId());
        msg.setSentTo(peerId);
        msg.setContent("Hello There");

        List<Message> msgList = new ArrayList<>();
        msgList.add(msg);
        return msgList;
    }

    public List<Message> getChatMessageList(String peerId, int msgNotOlderThanDays){
        return null;
    }

    public void sendFeedback(FeedbackType type, String feedback, ConnHandler handler){

    }
}
