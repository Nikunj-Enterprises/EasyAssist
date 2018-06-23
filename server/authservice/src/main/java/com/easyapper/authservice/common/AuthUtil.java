package com.easyapper.authservice.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.easyapper.authservice.common.AppConstants.AUTH_UTIL_FIELD_SEPARATOR;
import static com.easyapper.authservice.common.AppConstants.CONTEXT_ID_EXPIRY_MILLIS;

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
            String companyUserName = Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(contextId)
                    .getBody()
                    .getSubject();

            String[] tokens = companyUserName.split(AUTH_UTIL_FIELD_SEPARATOR);

            return tokens[1];
        }catch (Exception ex){
            return null;
        }
    }

    public static String getCompanyIdFromContextId(String contextId){
        if(contextId == null || contextId.isEmpty()){
            return null;
        }
        try{
            String companyUserName =  Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY)
                    .parseClaimsJws(contextId)
                    .getBody()
                    .getSubject();

            String[] tokens = companyUserName.split(AUTH_UTIL_FIELD_SEPARATOR);

            return tokens[0];
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
        Date expiryDate = new Date(sysTimeMillis + CONTEXT_ID_EXPIRY_MILLIS);

        return Jwts.builder()
                .setSubject(companyUserName)
                .setIssuedAt(issueDate)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET_KEY)
                .compact();
    }

    public static Boolean isContextIdValid(String contextId, UserDetails userDetails){
        String contextUserId = getUserIdFromContextId(contextId);
        String contextCompanyId = getCompanyIdFromContextId(contextId);
        String contextUserName = contextCompanyId+AUTH_UTIL_FIELD_SEPARATOR+contextUserId;
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
