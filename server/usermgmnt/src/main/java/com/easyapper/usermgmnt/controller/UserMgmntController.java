package com.easyapper.usermgmnt.controller;

import com.easyapper.usermgmnt.model.User;
import com.easyapper.usermgmnt.service.UserMgmntService;
import com.easyapper.usermgmnt.util.ResponseMessage;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping("/userservice")
public class UserMgmntController {
    private final UserMgmntService userSrv;

    @Autowired
    public UserMgmntController(UserMgmntService userSrv){
        this.userSrv = userSrv;
    }

    @RequestMapping(value= "/apps/{appId}/users", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> createUser(@PathVariable("appId") String appId,
                                                      @RequestBody User user){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.CREATED;
        if(appId == null || appId.isEmpty()
                || user.getUserId() == null || user.getUserId().isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }else {
            ObjectId id = userSrv.createUser(appId, user);

            resMsg.setId(id.toHexString());
            resMsg.setStatus("Success");
            resMsg.setMessage("User created successfully");
        }

        return new ResponseEntity<ResponseMessage>(resMsg, status);
    }

    @RequestMapping(value= "/apps/{appId}/users/{userId}", method = RequestMethod.PUT)
    public ResponseEntity<ResponseMessage> updateUser(@PathVariable("appId") String appId,
                                                      @PathVariable("userId") String userId,
                                                      @RequestBody User user){
return null;
    }

    @RequestMapping(value= "/apps/{appId}/users/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable("appId") String appId,
                                                      @PathVariable("userId") String userId){
return null;
    }

    @RequestMapping(value= "/apps/{appId}/users/{userId}", method = RequestMethod.GET)
    public User findUser(@PathVariable("appId") String appId,
                         @PathVariable("userId") String userId){
         return userSrv.findUserById(appId, userId);
    }

    @RequestMapping(value= "/apps/{appId}/users", method = RequestMethod.GET)
    public List<User> findUser(@PathVariable("appId") String appId){
         return userSrv.findAllUsers(appId);
    }
}
