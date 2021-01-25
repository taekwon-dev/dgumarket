package com.springboot.dgumarket.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.dgumarket.model.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by TK YOUN (2020-11-01 오후 1:32)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public class UserDetailsImpl implements UserDetails {

    private int id;

    private String nickName;

    private String webMail;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;


    public UserDetailsImpl(int id, String nickName, String password, String webMail, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nickName = nickName;
        this.password = password;
        this.webMail = webMail;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Member member) {
        List<GrantedAuthority> authorities = member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());


        // return (id, userNickName, webMail, authorities)
        return new UserDetailsImpl(
                member.getId(),
                member.getNickName(),
                member.getPassword(),
                member.getWebMail(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public int getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /** username = webMail (webMail 고유 값 개념) */
    @Override
    public String getUsername() {
        return webMail;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
