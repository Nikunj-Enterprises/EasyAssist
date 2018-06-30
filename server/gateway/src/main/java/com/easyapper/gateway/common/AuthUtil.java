package com.easyapper.gateway.common;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import static com.easyapper.gateway.common.AppConstants.AUTH_UTIL_FIELD_SEPARATOR;
import static com.easyapper.gateway.common.AppConstants.CONTEXT_ID_EXPIRY_MILLIS;


@Component
public class AuthUtil {

    private static final String JWT_SECRET_KEY = "easyApperSecretKey";

    // class is meant to be used static way
    private AuthUtil(){

    }

    public static String getUserIdFromContextId(String contextId){
        if(contextId == null || contextId.isEmpty()){
            return null;
        }
        try {
            return Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(contextId)
                    .getBody()
                    .getSubject();
        }catch (Exception ex){
            return null;
        }
    }


    public static Date getCreatedDateFromContextId(String contextId){
        if(contextId == null || contextId.isEmpty()){
            return null;
        }
        return Jwts.parser()
                   .setSigningKey(JWT_SECRET_KEY)
                   .parseClaimsJws(contextId)
                   .getBody()
                   .getIssuedAt();
    }

    public static Date getExpirationDateFromContextId(String contextId) {
        if(contextId == null || contextId.isEmpty()){
            return null;
        }
        return Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY)
                .parseClaimsJws(contextId)
                .getBody()
                .getExpiration();
    }

    public static String generateContextId(UserDetails userDetails) {
        if(userDetails == null){
            return null;
        }

        String companyUserName = userDetails.getUsername();
        Long sysTimeMillis = System.currentTimeMillis();
        Date issueDate = new Date(sysTimeMillis);
        Date expiryDate = new Date(CONTEXT_ID_EXPIRY_MILLIS);

        return Jwts.builder()
                .setSubject(companyUserName)
                .setIssuedAt(issueDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET_KEY)
                .compact();
    }

    public static Boolean isContextIdValid(String contextId, UserDetails userDetails){
        String contextUserName = getUserIdFromContextId(contextId);

        Date issueDate = getCreatedDateFromContextId(contextId);
        Date expiryDate = getExpirationDateFromContextId(contextId);

        Date currentDate = new Date();
        if(currentDate.after(issueDate)&&currentDate.before(expiryDate)
                && userDetails.getUsername().equals(contextUserName)){
            return  true;
        }
        return false;
    }
}
