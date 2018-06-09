package com.example.test;

import com.example.bloodline.Message;
import com.example.bloodline.Peer;
import com.example.bloodline.ProcessManager;
import com.example.bloodline.StateChangeNotifier;

public class ChangeNotifier implements StateChangeNotifier {
    private ProcessManager mgr;
    public ChangeNotifier(ProcessManager mgr){
        this.mgr = mgr;
    }
    @Override
    public void onProcessMgrListChange(String listType) {
        System.out.println("onProcessMgrListChange called with arg:"+listType);
        if("message".equals(listType)) {
            for(Message msg :mgr.getReqMsgList()){
                System.out.println(msg.toString());
            }
        }
        if("peer".equals(listType)) {
            for (Peer peer : mgr.getDonorList()){
                System.out.println(peer.toString());
            }
        }
    }

    @Override
    public void onPeerMsgListChange(String peerId) {
        System.out.println("onPeerMsgListChange called for peer :"+peerId);
        for(Peer peer : mgr.getDonorList()){
            if(peer.getPeerId().equals(peerId)){
                for(String msg : peer.getMsgList()){
                    System.out.println(msg);
                }

                break;
            }
        }
    }
}
