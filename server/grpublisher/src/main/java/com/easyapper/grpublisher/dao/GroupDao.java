package com.easyapper.grpublisher.dao;

import com.easyapper.grpublisher.model.Group;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Repository
public class GroupDao {
    private final MongoOperations mongoTemplate;

    @Autowired
    public GroupDao(MongoOperations mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    // DB <appId_groups> structure is
    //  userId  | groupName  | createdAt | validTill
    // uniquekey => {userId, groupName, createdAt}
    public ObjectId createGroup(String appId, String grpName, String createdBy){
         String tabName = appId + "_groups";
         long createdAt = Date.from(Instant.now()).getTime();
         long validTill = Long.MAX_VALUE;
         DBObject dbObj = new BasicDBObject();
         dbObj.put("createdBy", createdBy);
         dbObj.put("groupName", grpName);
         dbObj.put("createdAt",createdAt);
         dbObj.put("validTill",validTill);

         mongoTemplate.insert(dbObj, tabName);
         Group grp = addGroupIdField(appId, grpName, createdBy, createdAt);
         return new ObjectId(grp.getGroupId());
    }

    public void addGroupMember(String appId, ObjectId grpId, String createdBy){
        String tabName = appId + "_groups";
        long createdAt = Date.from(Instant.now()).getTime();
        long validTill = Long.MAX_VALUE;
        Group grp = findGroupById(appId, grpId);

        DBObject dbObj = new BasicDBObject();
        dbObj.put("createdBy", createdBy);
        dbObj.put("groupId", grpId);
        dbObj.put("groupName", grp.getGroupName());
        dbObj.put("createdAt",createdAt);
        dbObj.put("validTill",validTill);

        mongoTemplate.insert(dbObj, tabName);
    }

    public void removeGroupMember(String appId, ObjectId grpId, String memberId){
        String tabName = appId + "_groups";
        Query query = new Query();
        query.addCriteria(Criteria.where("groupId").is(grpId).and("createdBy").is(memberId));

        Update update = new Update();
        long currentTS = Date.from(Instant.now()).getTime();
        update.set("validTill", currentTS);

        mongoTemplate.updateFirst(query, update, tabName);
    }

    public Group findGroupById(String appId, ObjectId grpId){
        String tabName = appId + "_groups";

        Query query = new Query();
        query.addCriteria(Criteria.where("groupId").is(grpId));

        Group grp = mongoTemplate.findOne(query, Group.class, tabName);
        return grp;
    }

    public List<String> findMembersOfGroup(String appId, ObjectId grpId){
        String tabName = appId + "_groups";
        long currentTS = Date.from(Instant.now()).getTime();
        List<String>  memberIds = new ArrayList<>();

        Query query = new Query();
        query.fields().include("createdBy");
        query.fields().exclude("_id");

        query.addCriteria(
                Criteria.where("groupId").is(grpId)
                        .and("validTill").gt(currentTS)
        );

        mongoTemplate.find(query, Group.class, tabName).forEach(
                e ->{
                    memberIds.add(e.getCreatedBy());
                }
        );

        return memberIds;
    }

    public List<Group> findGroupsByMember(String appId, String memberId){
        String tabName = appId + "_groups";
        long currentTS = Date.from(Instant.now()).getTime();
        Query query = new Query();
        query.addCriteria(
                Criteria.where("createdBy").is(memberId)
                        .and("validTill").gt(currentTS)
        );

        return mongoTemplate.find(query, Group.class, tabName);
    }

    private Group addGroupIdField(String appId, String grpName, String memberId, long createdAt){
        String tabName = appId + "_groups";

        Group grp = new Group();
        grp.setCreatedAt(createdAt);
        grp.setCreatedBy(memberId);
        grp.setGroupName(grpName);

        Query query = new Query();
        query.addCriteria(
                Criteria.where("groupName").is(grpName)
                        .and("createdBy").is(memberId)
                        .and("createdAt").is(createdAt)
        );

        Update update = new Update();

        for(Document obj : mongoTemplate.getCollection(tabName).find()){
            update.set("groupId",obj.get("_id"));
            grp.setGroupId((ObjectId) obj.get("_id"));
            break;
        }

        mongoTemplate.getCollection(tabName).findOneAndUpdate(query.getQueryObject(), update.getUpdateObject());

        return grp;
    }

    public List<Group> findGroups(String appId){
        String tabName = appId + "_groups";
        long currentTS = Date.from(Instant.now()).getTime();
        List<Group> groups = new ArrayList<>();

        Query query = new Query();
        query.addCriteria(Criteria.where("validTill").gt(currentTS));
        query.fields().include("groupId");
        //query.fields().include("groupName");


       /* Aggregation agg = newAggregation(
                match(Criteria.where("validTill").gt(currentTS)),
                group("groupId", "groupName"),
                project("groupId", "groupName")
        );*/

        //return mongoTemplate.aggregate(agg, tabName, Group.class).getMappedResults();
        MongoCursor<ObjectId> itr =
                mongoTemplate
                        .getCollection(tabName)
                        .distinct("groupId", query.getQueryObject(), ObjectId.class)
                        .iterator();

       while (itr.hasNext()){
           groups.add(findGroupById(appId, itr.next()));
       }

        return groups;
    }

}
