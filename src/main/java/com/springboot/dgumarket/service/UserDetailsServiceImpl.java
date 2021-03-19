package com.springboot.dgumarket.service;

import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by TK YOUN (2020-11-01 오후 1:40)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String webMail) throws UsernameNotFoundException {

        Member member = memberRepository.findByWebMailAndIsWithdrawn(webMail, 0)
                // UsernameNotFoundException extends AuthenticationException -> AuthEntryPointJwt.class
                .orElseThrow(()-> new UsernameNotFoundException("User Not Found with Dongguk Univ's webMail : " + webMail));

        return UserDetailsImpl.build(member);
    }
}

