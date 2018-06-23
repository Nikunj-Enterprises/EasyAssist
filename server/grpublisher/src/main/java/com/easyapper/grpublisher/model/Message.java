package com.easyapper.grpublisher.model;

import java.util.Objects;

public class Message {
    private long msgId;
    private String sentBy;
    private String sentTo;
    private String content;
    private float senderLat;
    private float senderLang;
    // value of isBroadcast indicates message is sent to peer or group
    // true: sentTo is groupName, false:sentTo is peerId
    private boolean isBroadcast;
    private long createdAt;

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String message) {
        this.content = message;
    }

    public float getSenderLat() {
        return senderLat;
    }

    public void setSenderLat(float senderLat) {
        this.senderLat = senderLat;
    }

    public float getSenderLang() {
        return senderLang;
    }

    public void setSenderLang(float senderLang) {
        this.senderLang = senderLang;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return getMsgId() == message.getMsgId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getMsgId());
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", sentBy='" + sentBy + '\'' +
                ", sentTo='" + sentTo + '\'' +
                ", content='" + content + '\'' +
                ", senderLat=" + senderLat +
                ", senderLang=" + senderLang +
                ", isBroadcast=" + isBroadcast +
                ", createdAt=" + createdAt +
                '}';
    }
}
