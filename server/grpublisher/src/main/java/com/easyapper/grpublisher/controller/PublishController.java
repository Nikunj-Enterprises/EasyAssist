package com.easyapper.grpublisher.controller;

import com.easyapper.grpublisher.model.Group;
import com.easyapper.grpublisher.model.Message;
import com.easyapper.grpublisher.service.GroupService;
import com.easyapper.grpublisher.service.MessagingService;

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

import java.sql.Date;
import java.time.Instant;
import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping("/grpublisher")
public class PublishController {
    private final GroupService grpSrv;
    private final MessagingService msgSrv;

    @Autowired
    public PublishController(GroupService grpSrv, MessagingService msgSrv){
        this.grpSrv = grpSrv;
        this.msgSrv = msgSrv;
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> createGroup(@PathVariable("appId") String appId,
                                      @PathVariable("userId") String userId,
                                      @RequestBody String groupName){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.CREATED;
        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupName == null || groupName.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }else {
            String id = grpSrv.createGroup(appId, groupName, userId);

            resMsg.setId(id);
            resMsg.setStatus("Success");
            resMsg.setMessage("Group created successfully");
        }

        return new ResponseEntity<ResponseMessage>(resMsg, status);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}",
            method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> leaveGroup(@PathVariable("appId") String appId,
                            @PathVariable("userId") String userId,
                            @PathVariable("groupId") String groupId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupId == null || groupId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        try {
            ObjectId id = new ObjectId(groupId);
            if(grpSrv.getGroupById(appId, id) == null ){
                resMsg.setStatus("Failed");
                resMsg.setMessage("Group Not Found");
                status = HttpStatus.NOT_FOUND;
            }
        }catch (Throwable e){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        resMsg.setStatus("Success");
        resMsg.setMessage("Left group successfully");

        grpSrv.removeGroupMemberMapping(appId, new ObjectId(groupId), userId);
        return new ResponseEntity<ResponseMessage>(resMsg, status);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}/members",
            method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> addGroupMember(@PathVariable("appId") String appId,
                               @PathVariable("userId") String userId,
                               @PathVariable("groupId") String groupId,
                               @RequestBody String memberId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.CREATED;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupId == null || groupId.isEmpty()
                || memberId == null || memberId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }

        grpSrv.addGroupMemberMapping(appId, new ObjectId(groupId), memberId);
        resMsg.setId(memberId);
        resMsg.setStatus("Success");
        resMsg.setMessage("Member:"+memberId+" added to group successfully");

        return new ResponseEntity<ResponseMessage>(resMsg, status);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}/members/{memberId}",
            method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> removeGroupMember(@PathVariable("appId") String appId,
                                  @PathVariable("userId") String userId,
                                  @PathVariable("groupId") String groupId,
                                  @PathVariable("memberId") String memberId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupId == null || groupId.isEmpty()
                || memberId == null || memberId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        try {
            ObjectId id = new ObjectId(groupId);
            if(grpSrv.getGroupById(appId, id) == null ){
                resMsg.setStatus("Failed");
                resMsg.setMessage("Group Not Found");
                status = HttpStatus.NOT_FOUND;
            }
        }catch (Throwable e){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        grpSrv.removeGroupMemberMapping(appId, new ObjectId(groupId), memberId);

        resMsg.setStatus("Success");
        resMsg.setMessage("Member removed from group successfully");

        return new ResponseEntity<ResponseMessage>(resMsg, status);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups", method = RequestMethod.GET)
    public List<Group> findGroupsForMember(@PathVariable("appId") String appId,
                                           @PathVariable("userId") String userId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        return grpSrv.getGroupsForMember(appId, userId);
    }

    @RequestMapping(value = "/apps/{appId}/groups", method = RequestMethod.GET)
    public List<Group> findGroups(@PathVariable("appId") String appId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        return grpSrv.getGroups(appId);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}/members", method = RequestMethod.GET)
    public List<String> findGroupMembers(@PathVariable("appId") String appId,
                                         @PathVariable("userId") String userId,
                                         @PathVariable("groupId") String groupId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupId == null || groupId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        try {
            ObjectId id = new ObjectId(groupId);
            if(grpSrv.getGroupById(appId, id) == null ){
                resMsg.setStatus("Failed");
                resMsg.setMessage("Group Not Found");
                status = HttpStatus.NOT_FOUND;
            }
        }catch (Throwable e){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }

        return grpSrv.getMembersOfGroup(appId, new ObjectId(groupId));
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}", method = RequestMethod.GET)
    public Group findGroupById(@PathVariable("appId") String appId,
                                         @PathVariable("userId") String userId,
                                         @PathVariable("groupId") String groupId){
        ResponseMessage resMsg = new ResponseMessage();
        HttpStatus status = HttpStatus.OK;

        if(appId == null || appId.isEmpty()
                || userId == null || userId.isEmpty()
                || groupId == null || groupId.isEmpty()){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }
        Group grp = null;
        try {
            ObjectId id = new ObjectId(groupId);
            grp = grpSrv.getGroupById(appId, id);
            if(grp == null ){
                resMsg.setStatus("Failed");
                resMsg.setMessage("Group Not Found");
                status = HttpStatus.NOT_FOUND;
            }
        }catch (Throwable e){
            resMsg.setStatus("Failed");
            resMsg.setMessage("Mandatory field missing");
            status = HttpStatus.BAD_REQUEST;
        }

        return grp;
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/groups/{groupId}/message",
            method = RequestMethod.POST)
    public void sendMsgToGroup(@PathVariable("appId") String appId,
                               @PathVariable("userId") String userId,
                               @PathVariable("groupId") String groupId,
                               @RequestBody Message message){
        message.setBroadcast(true);
        message.setSentBy(userId);
        message.setSentTo(groupId);
        message.setCreatedAt( Date.from(Instant.now()).getTime());
        String msgStr = message.toString();

        List<String> members = grpSrv.getMembersOfGroup(appId, new ObjectId(groupId));
        members.parallelStream()
                .forEach( e -> msgSrv.publishMessage(appId, e, msgStr));
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}/peers/{peerId}/message",
            method = RequestMethod.POST)
    public void sendMsgToPeer(@PathVariable("appId") String appId,
                              @PathVariable("userId") String userId,
                              @PathVariable("peerId") String peerId,
                              @RequestBody Message message){
        message.setBroadcast(false);
        message.setSentBy(userId);
        message.setSentTo(peerId);
        String msgStr = message.toString();

        msgSrv.publishMessage(appId, userId, msgStr);
    }
}
