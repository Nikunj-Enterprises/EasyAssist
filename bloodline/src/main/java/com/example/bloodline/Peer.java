package com.example.bloodline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Peer {
    private String peerId;
    private float lat;
    private float lang;
    private List<String> msgList = new ArrayList<>();

    public Peer(){

    }

    public Peer(String peerId){
        this.peerId = peerId;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLang() {
        return lang;
    }

    public void setLang(float lang) {
        this.lang = lang;
    }

    public List<String> getMsgList() {
        return msgList;
    }

    public int getLastReadMsgIndex() {
        return 0;
    }

    public void addMessage(String msg){
        this.msgList.add(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peer)) return false;
        Peer peer = (Peer) o;
        return Objects.equals(getPeerId(), peer.getPeerId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getPeerId());
    }

    @Override
    public String toString() {
        return "Peer{" +
                "peerId='" + peerId + '\'' +
                ", lat=" + lat +
                ", lang=" + lang +
                ", msgList=" + msgList +
                '}';
    }
}
