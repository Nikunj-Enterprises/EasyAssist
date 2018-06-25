package com.easyapper.bloodline;

public interface StateChangeNotifier {
    public void onProcessMgrListChange(String listType); // listType could be 'message' or 'peer'
    public void onPeerMsgListChange(String peerId);
}
