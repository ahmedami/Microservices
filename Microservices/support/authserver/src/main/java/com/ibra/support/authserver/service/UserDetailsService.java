package com.ibra.support.authserver.service;

import com.ibra.support.authserver.repository.AuthService;
import com.visualmeta.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;


@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    @Autowired
    private AuthService authService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {

        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();

        User userFromDatabase;
        userFromDatabase = authService.findByEmail(lowercaseLogin);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(userFromDatabase.getRole().toString());
        grantedAuthorities.add(grantedAuthority);


        return new org.springframework.security.core.userdetails.User(userFromDatabase.getEmail(), userFromDatabase.getPassword(), grantedAuthorities);

    }

}
