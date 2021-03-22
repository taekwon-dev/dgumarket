package com.springboot.dgumarket.repository.member.redis;

import com.springboot.dgumarket.model.member.redis.RedisJwtToken;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@EnableRedisRepositories
public interface RedisJwtTokenRepository extends CrudRepository<RedisJwtToken, String> {
}
