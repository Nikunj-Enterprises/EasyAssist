package com.easyapper.gateway.controller;


import com.easyapper.gateway.common.AppContext;
import com.easyapper.gateway.common.AuthUtil;
import com.easyapper.gateway.common.ContextHolder;
import com.easyapper.gateway.exception.AuthServiceException;
import com.easyapper.gateway.model.RegisteredUserDetail;
import com.easyapper.gateway.service.UserRegistrationService;
import com.easyapper.gateway.util.PasswordChangeRequest;
import com.easyapper.gateway.util.ResponseMessage;
import com.easyapper.gateway.util.UserAuthenticationRequest;
import com.easyapper.gateway.util.UserAuthenticationResponse;
import com.easyapper.gateway.util.UserValidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static com.easyapper.gateway.common.AppConstants.CONTEXT_ID_EXPIRY_MILLIS;

@CrossOrigin
@EnableAutoConfiguration
@RestController
@RequestMapping("/authservice")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private UserRegistrationService registrationService;

    @Autowired
    public AuthController(UserDetailsService userDetailsService,
                          UserRegistrationService registrationService) {
       // this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.registrationService = registrationService;
    }

    @RequestMapping(value = "/apps/{appId}/register", method = RequestMethod.POST,
    consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserAuthenticationResponse> registerUser(
            @PathVariable("appId") String appId,
            @RequestBody RegisteredUserDetail user
    ){
        ContextHolder.setAppContext(new AppContext(appId));

        registrationService.registerUser(appId, user);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String contextId = AuthUtil.generateContextId(userDetails);

        UserAuthenticationResponse responseObj = new UserAuthenticationResponse();
        responseObj.setContextId(contextId);
        responseObj.setContextIdExpiryMillis(CONTEXT_ID_EXPIRY_MILLIS);
        return ResponseEntity.status(201).body(responseObj);
    }

    @RequestMapping(value = "/apps/{appId}/authenticate", method = RequestMethod.POST)
    public ResponseEntity<UserAuthenticationResponse> authenticateUser(
            @PathVariable("appId") String appId,
            @RequestBody UserAuthenticationRequest authRequest
    ){
        ContextHolder.setAppContext(new AppContext(appId));
        String userId = authRequest.getUserId();

        UserAuthenticationResponse responseObj = new UserAuthenticationResponse();
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            authRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
            String contextId = AuthUtil.generateContextId(userDetails);
            responseObj.setContextId(contextId);
            responseObj.setContextIdExpiryMillis(CONTEXT_ID_EXPIRY_MILLIS);
            return ResponseEntity.ok(responseObj);
        }catch (Exception ex){
            log.debug(ex.getMessage());
            AuthServiceException e = new AuthServiceException("Authentication Failed", ex);
            e.setStatusCode(401);
            throw e;
        }
    }

    @RequestMapping(value = "/apps/{appId}/refresh", method = RequestMethod.POST)
    public ResponseEntity<UserAuthenticationResponse> refreshAuthToken(
            @PathVariable("appId") String appId,
            @RequestHeader("authToken") String  authToken,
            @RequestBody String userId
    ){
        ContextHolder.setAppContext(new AppContext(appId));
        UserAuthenticationResponse responseObj = new UserAuthenticationResponse();
        try {
            if(userId.equals(AuthUtil.getUserIdFromContextId(authToken)) &&
               appId.equals(AuthUtil.getCompanyIdFromContextId(authToken))) {

                log.debug(" refresh AuthToken for userId :{} , companyId : {}", userId, appId);

                final UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                if (!AuthUtil.isContextIdValid(authToken, userDetails)) {
                    throw new AuthServiceException("Access Denied");
                }
                String newContextId = AuthUtil.generateContextId(userDetails);
                responseObj.setContextId(newContextId);
                responseObj.setContextIdExpiryMillis(CONTEXT_ID_EXPIRY_MILLIS);
                return ResponseEntity.ok(responseObj);
            }
        }catch (Exception ex){
            log.debug(ex.getMessage());
        }
        AuthServiceException e = new AuthServiceException("Authentication Failed");
        e.setStatusCode(401);
        throw e;
    }

    @RequestMapping(value = "/apps/{appId}/validate", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> validateUser(
            @PathVariable("appId") String appId,
            @RequestBody UserValidationRequest validationRequest
    ){
        ContextHolder.setAppContext(new AppContext(appId));
        String contextId = validationRequest.getToken();
        String userId = validationRequest.getUserId();
        String requiredRole = validationRequest.getRequiredRoleName();
        ResponseMessage responseMessage  = new ResponseMessage();

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);

        if (AuthUtil.isContextIdValid(contextId, userDetails)) {
            Collection<SimpleGrantedAuthority> authorities =
                    (Collection<SimpleGrantedAuthority>) userDetails.getAuthorities();
            if(authorities.contains(new SimpleGrantedAuthority(requiredRole))){
                responseMessage.setStatus("Success");
                responseMessage.setMessage("valid");
                return ResponseEntity.ok(responseMessage);
            }
        }
        responseMessage.setStatus("Failed");
        responseMessage.setMessage("invalid");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
    }

    @RequestMapping(value = "/apps/{appId}/users/{userId}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> changeUserPassword(
            @PathVariable("appId") String appId,
            @PathVariable("userId") String userId,
            @RequestHeader("authToken") String  authToken,
            @RequestBody PasswordChangeRequest changeRequest
    ){
        ContextHolder.setAppContext(new AppContext(appId));
        //TODO
        return null;
    }
}
