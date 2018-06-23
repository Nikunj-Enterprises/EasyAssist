package com.easyapper.usermgmnt.dao;

import com.easyapper.usermgmnt.model.User;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.easyapper.usermgmnt.common.AppConstants.*;


@Repository
public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final MongoOperations mongoTemplate;

    @Autowired
    public UserDao(MongoOperations ops){
        Objects.requireNonNull(ops, "mongoTemplate object must not be null");
        this.mongoTemplate = ops;
    }

    public User getUserById(String appId, String userId){
        String tabName = appId + "_users";
        long currentTS = Date.from(Instant.now()).getTime();

        if(userId != null && appId != null) {
            Query query = new Query();
            query.addCriteria(
                    Criteria.where(USERID).is(userId)
                            .and(VALID_TILL).gt(currentTS)
            );

            return mongoTemplate.findOne(query, User.class, tabName);
        }
        throw new RuntimeException("user mandatory info not provided");
    }

    public ObjectId createUser(String appId, User user){
        if(user != null
                && user.getUserId() != null && user.getUserId().isEmpty()){
            String tabName = appId + "_users";
            long createdAt = Date.from(Instant.now()).getTime();
            long validTill = Long.MAX_VALUE;
            User aUser = getUserById(appId, user.getUserId());

            if(aUser == null){
                user.setCreatedAt(createdAt);
                user.setValidTill(validTill);

                mongoTemplate.insert(user, tabName);
                aUser = getUserById(appId, user.getUserId());
                return aUser.getId();
            }else{
                throw new RuntimeException(String.format("userId: %s, is already taken", user.getUserId()));
            }
        }

        throw new RuntimeException("user mandatory info not provided");
    }

    public void updateUser(String appId, User user){
        String tabName = appId + "_users";
        long currentTS = Date.from(Instant.now()).getTime();

        Query query = new Query();
        query.addCriteria(
                Criteria.where(USERID).is(user.getUserId())
                        .and(VALID_TILL).gt(currentTS)
        );
        Update update = new Update();
        if(user.getEmail() != null && !user.getEmail().isEmpty() ){ update.set(EMAIL,user.getEmail());}
        if(user.getFirstName() != null && !user.getFirstName().isEmpty()){ update.set(FIRSTNAME,user.getFirstName());}
        if(user.getLastName() != null && !user.getLastName().isEmpty()){ update.set(LASTNAME,user.getLastName());}
        if(user.getMobileNumber() != null && user.getMobileNumber() != 0){ update.set(MOBILENUMBER,user.getMobileNumber());}
        if(user.getAddressFirst() != null && !user.getAddressFirst().isEmpty() ){ update.set(ADDRESS_FIRST_LINE,user.getAddressFirst());}
        if(user.getAddressSecond() != null && !user.getAddressSecond().isEmpty() ){ update.set(ADDRESS_SECOND_LINE,user.getAddressSecond());}
        if(user.getStateName() != null && !user.getStateName().isEmpty() ){ update.set(STATE_NAME,user.getStateName());}
        if(user.getCountry() != null && !user.getCountry().isEmpty() ){ update.set(COUNTRY,user.getCountry());}
        if(user.getCityName() != null && !user.getCityName().isEmpty() ){ update.set(CITY_NAME,user.getCityName());}
        if(user.getZipCode() != null && Integer.toString(user.getZipCode()).length() < 5 ){ update.set(ZIP_CODE,user.getZipCode());}

        mongoTemplate.updateFirst(query, update, User.class, tabName);
    }

    public List<User> getAllUsers(String appId){
        String tabName = appId + "_users";
        long currentTS = Date.from(Instant.now()).getTime();

        Query query = new Query();
        query.addCriteria(
                Criteria.where(VALID_TILL).gt(currentTS)
        );

        List<User> users = mongoTemplate.find(query, User.class, tabName);
        return users == null ? new ArrayList<>(): users;
    }

    public void deleteUser(String appId, String userId){
        String tabName = appId + "_users";
        long currentTS = Date.from(Instant.now()).getTime();

        Query query = new Query();
        query.addCriteria(
                Criteria.where(USERID).is(userId)
                        .and(VALID_TILL).gt(currentTS)
        );
        Update update = new Update();
        update.set(VALID_TILL, currentTS);

        mongoTemplate.updateFirst(query, update, tabName);
    }
}
