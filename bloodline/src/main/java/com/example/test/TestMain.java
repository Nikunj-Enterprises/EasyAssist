package com.example.test;

import com.example.bloodline.BloodGroup;
import com.example.bloodline.ConnHandler;
import com.example.bloodline.Message;
import com.example.bloodline.ProcessManager;
import com.example.bloodline.User;

public class TestMain {
    public static void main(String[] args){
        ProcessManager mgr = new ProcessManager();
        mgr.setLocationReader(new Locater());
        mgr.setStateChangeNotifier(new ChangeNotifier(mgr));
        User user = new User();
        user.setUserId("ABC123");
        user.setBloodGroup(BloodGroup.O2);
        user.setName("One user");
        user.setPassword("secret");

        mgr.connect(user, new MainCBHandler());

        Message broadcast = new Message();
        broadcast.setContent("Need O-ve blood for 5 year old girl");
        broadcast.setSentTo(BloodGroup.O2.getValue());
        mgr.broadcastReq(broadcast, new BroadcastCBHandler());

    }

    static class MainCBHandler implements ConnHandler{

        @Override
        public void OnSuccess(int status, String token) {
            System.out.println("Success Status :"+status+"\n Token:"+token);
        }

        @Override
        public void OnFailure(int status, String message) {
            System.out.println("Failed Status :"+status+"\n Message:"+message);
        }
    }

    static class BroadcastCBHandler implements ConnHandler{

        @Override
        public void OnSuccess(int status, String token) {
            System.out.println("Success Status :"+status+"\n Broadcast Token:"+token);
        }

        @Override
        public void OnFailure(int status, String message) {
            System.out.println("Failed Status :"+status+"\n Message:"+message);
        }
    }
}
