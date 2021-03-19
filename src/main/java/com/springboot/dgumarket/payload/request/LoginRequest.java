package com.springboot.dgumarket.payload.request;

import javax.validation.constraints.NotBlank;

/**
 * Created by TK YOUN (2020-11-01 오후 2:11)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public class LoginRequest {

    @NotBlank
    private String webMail;

    @NotBlank
    private String password;


    public String getWebMail() {
        return webMail;
    }

    public void setWebMail(String webMail) {
        this.webMail = webMail;
    }

    public String getPassword() {
        return password;
    }

}
