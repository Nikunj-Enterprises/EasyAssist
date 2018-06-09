package com.example.grpublisher.service;

import com.example.grpublisher.dao.GroupDao;
import com.example.grpublisher.model.Group;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    private final GroupDao grpDao;

    @Autowired
    public GroupService(GroupDao grpDao){
        this.grpDao = grpDao;
    }

    public String createGroup(String appId, String grpName, String userId){
        ObjectId id = grpDao.createGroup(appId, grpName, userId);
        return id.toHexString();
    }

    public void addGroupMemberMapping(String appId, ObjectId grpId, String userId){
        List<Group> grps = grpDao.findGroupsByMember(appId, userId);
        Group aGrp = grpDao.findGroupById(appId, grpId);

        if(grps.contains(aGrp)){
            throw new RuntimeException(String.format("User %s is already subscribed to Group %s", userId, aGrp.getGroupName()));
        }

        grpDao.addGroupMember(appId, grpId, userId);
    }

    public void removeGroupMemberMapping(String appId, ObjectId grpId, String userId){
        List<Group> grps = grpDao.findGroupsByMember(appId, userId);
        Group aGrp = grpDao.findGroupById(appId, grpId);

        if(!grps.contains(aGrp)){
            throw new RuntimeException(String.format("User %s is not subscribed to group %s", userId, aGrp.getGroupName()));
        }

        grpDao.removeGroupMember(appId, grpId, userId);
    }

    public List<String> getMembersOfGroup(String appId, ObjectId grpId){
        List<String> memberIDs = grpDao.findMembersOfGroup(appId, grpId);
        return  memberIDs == null? new ArrayList<>() : memberIDs;
    }

    public List<Group> getGroupsForMember(String appId, String memberId){
        List<Group> groups = grpDao.findGroupsByMember(appId, memberId);
        return  groups == null? new ArrayList<>() : groups;
    }

    public List<Group> getGroups(String appId){
        List<Group> groups = grpDao.findGroups(appId);
        return  groups == null? new ArrayList<>() : groups;
    }

    public Group getGroupById(String appId, ObjectId id) {
        return grpDao.findGroupById(appId, id);
    }
}
