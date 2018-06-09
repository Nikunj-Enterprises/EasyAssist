package com.example.grpublisher.dao;

import com.example.grpublisher.model.Message;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageDao {

    public long logMessage(String appId, Message msg){
        return 0L;
    }

    public Message findMessageById(String appId, long msgId){
        return  null;
    }
}
