package com.easyapper.authservice.dao;

import com.easyapper.authservice.exception.AuthServiceException;
import com.easyapper.authservice.model.RegisteredUserDetail;
import com.easyapper.authservice.model.UserRoles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.easyapper.authservice.common.AppConstants.AUTH_UTIL_FIELD_SEPARATOR;

@Repository
public class UserDao {

    private final MongoOperations mongoTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDao(MongoOperations mongoTemplate, PasswordEncoder passwordEncoder){
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisteredUserDetail getUserById(String appId, String userId){
        if(userId != null && appId != null && !appId.isEmpty() && !userId.isEmpty()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(userId).and("isEnabled").is(true));

            return mongoTemplate.findOne(query,RegisteredUserDetail.class,appId+"_users");
        }
        return null;
    }

    public List<String> getUserRoles(String appId, String userId) {
        RegisteredUserDetail userDetail = getUserById(appId, userId);
        List<String> roles = new ArrayList<>();
        if(userDetail != null){
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(userId).and("isEnabled").is(true));
            UserRoles userRoles = mongoTemplate.findOne(query,UserRoles.class,appId+"_users");
            if(userRoles != null)
               roles.addAll(Arrays.asList(userRoles.getRoles()));
        }
        return roles;
    }

    public void createUser(String appId, RegisteredUserDetail user) {
        if(user.getUsername()== null || user.getPassword() == null
                || user.getUsername().isEmpty() || user.getPassword().isEmpty()){
            throw new AuthServiceException("mandatory field missing");
        }
        RegisteredUserDetail aUserDetail =
                new RegisteredUserDetail(
                        user.getUsername(), passwordEncoder.encode(user.getPassword())
                );
        aUserDetail.setAccountNonExpired(true);
        aUserDetail.setEnabled(true);
        aUserDetail.setAccountNonLocked(true);
        aUserDetail.setCredentialsNonExpired(true);

        mongoTemplate.insert(aUserDetail, appId+"_users");
    }
}
