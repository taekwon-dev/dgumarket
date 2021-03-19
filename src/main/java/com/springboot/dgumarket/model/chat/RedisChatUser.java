package com.springboot.dgumarket.model.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@Setter
@ToString
public class RedisChatUser {
    private String sessionId;
    private int userId;

    @Builder
    public RedisChatUser(String sessionId, int userId) {
        Assert.hasText(sessionId, "sessionId must have value");
        this.sessionId = sessionId;
        this.userId = userId;
    }
}
