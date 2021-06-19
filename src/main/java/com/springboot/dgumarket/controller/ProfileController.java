package com.springboot.dgumarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProfileController {

    public final Environment env;

    @GetMapping("/profile")
    public String profile(){
        env.getActiveProfiles();
        List<String> profiles = Arrays.asList(env.getActiveProfiles()); // 현재 활성화 중인 모든 profiles 체크합니다.
        for(String profile : profiles) {
            System.out.println("profile = " + profile);
        }
        return profiles.get(0);
    }
}
