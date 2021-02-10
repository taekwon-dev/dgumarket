package com.springboot.dgumarket.security.authentication;

import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by TK YOUN (2020-11-01 오후 3:51)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 검증을 위한 구현
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = (String)authentication.getCredentials();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        logger.debug("userDetails.getUsername() : " + userDetails.getUsername());
        logger.debug("userDetails.getPassword() : " + userDetails.getPassword());

        // password 일치하지 않으면!
        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            throw new BadCredentialsException("BadCredentialsException");
        }

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(
                userDetails, // 핵심 부분
                null,
                userDetails.getAuthorities());

        return authenticationToken;
    }

    // 토큰 타입과 일치할 때 인증
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}