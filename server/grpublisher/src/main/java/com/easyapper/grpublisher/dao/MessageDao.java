package com.easyapper.grpublisher.dao;

import com.easyapper.grpublisher.model.Message;

import org.springframework.stereotype.Repository;

@Repository
public class MessageDao {

    public long logMessage(String appId, Message msg){
        return 0L;
    }

    public Message findMessageById(String appId, long msgId){
        return  null;
    }
}
