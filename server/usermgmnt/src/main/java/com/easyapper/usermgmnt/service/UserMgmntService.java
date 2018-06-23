package com.easyapper.usermgmnt.service;

import com.easyapper.usermgmnt.dao.UserDao;
import com.easyapper.usermgmnt.model.User;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMgmntService {
    private final UserDao userDao;

    @Autowired
    public UserMgmntService(UserDao userDao){
        this.userDao = userDao;
    }

    public ObjectId createUser(String appId, User user){
        return userDao.createUser(appId, user);
    }

    public void updateUser(String appId, User user){
        userDao.updateUser(appId, user);
    }

    public void deleteUser(String appId, User user){
        userDao.deleteUser(appId, user.getUserId());
    }

    public User findUserById(String appId, String userId){
        return  userDao.getUserById(appId, userId);
    }

    public List<User> findAllUsers(String appId){
        return userDao.getAllUsers(appId);
    }
}
