package com.springboot.dgumarket.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by TK YOUN (2020-11-01 오후 2:13)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Getter
@Setter
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }


}