package com.example.grpublisher.model;

import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Group {
    private ObjectId groupId;
    private String groupName;
    private String createdBy;
    private long createdAt;

    private Set<String> grpMembers = new HashSet<>();

    public Group(){

    }

    public Group(String grpName, String createdBy){
        this.groupName = grpName;
        this.createdBy = createdBy;
    }

    public String getGroupId() {
        return groupId.toHexString();
    }

    public void setGroupId(ObjectId id) {
        this.groupId = id;
    }

    public void setGroupId(String id) {
        this.groupId = new ObjectId(id);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void addGroupMember(String userId){
        this.grpMembers.add(userId);
    }

    public void removeGroupMember(String userId){
        if(this.grpMembers.contains(userId)){
            this.grpMembers.remove(userId);
        }
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
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return Objects.equals(groupId, group.groupId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId);
    }
}
