package com.easyapper.authservice.service;

import com.easyapper.authservice.dao.UserDao;
import com.easyapper.authservice.model.RegisteredUserDetail;
import com.easyapper.authservice.common.ContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDao userDao;

    @Autowired
    public UserDetailsServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }


    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        String appId = ContextHolder.getAppContext().getAppId();
        RegisteredUserDetail user = userDao.getUserById(appId, userId);
        if(user == null){
            throw new RuntimeException(String.format("%s::userId '%s' not found.", appId, userId));
        }else{
            List<String> roles = userDao.getUserRoles(appId, userId);
            user.setAuthorities(getGrantedAuthoritiesList(roles));
            return user;
        }
    }

    private List<GrantedAuthority> getGrantedAuthoritiesList(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
    }
}
