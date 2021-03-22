package com.springboot.dgumarket.model.member.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Builder
@Getter
@RedisHash("refreshToken")
public class RedisJwtToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    // `로그인 상태 유지` 선택 여부 {true : 선택, false : 미선택}
    private boolean isKeepLoggedInState = false;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long timeToLive;

}
