package com.example.bloodline;

import java.util.Objects;

public class Message {
    private long msgId;
    private String sentBy;
    private String sentTo;
    private float lat;
    private float lang;
    private String content;
    // value of isBroadcast indicates message is sent to peer or group
    // true: sentTo is groupName, false:sentTo is peerId
    private boolean isBroadcast;
    private long createdAt;  //epoch millis

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
                ", lat=" + lat +
                ", lang=" + lang +
                ", content='" + content + '\'' +
                ", isBroadcast=" + isBroadcast +
                ", createdAt=" + createdAt +
                '}';
    }
}
