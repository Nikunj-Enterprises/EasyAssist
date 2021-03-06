package com.easyapper.authservice.service;

import com.easyapper.authservice.dao.UserDao;
import com.easyapper.authservice.model.RegisteredUserDetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {
    private final UserDao userDao;

    @Autowired
    public UserRegistrationService(UserDao userDao){
        this.userDao = userDao;
    }
    public void registerUser(String appId, RegisteredUserDetail user) {
        RegisteredUserDetail userDetail = userDao.getUserById(appId, user.getUsername());
        if(userDetail != null){
            throw new RuntimeException("userId already taken");
        }
        userDao.createUser(appId, user);
    }
}
