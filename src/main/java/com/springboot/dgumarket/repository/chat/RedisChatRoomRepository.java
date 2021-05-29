package com.springboot.dgumarket.repository.chat;

import com.springboot.dgumarket.model.chat.RedisChatRoom;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableRedisRepositories
public interface RedisChatRoomRepository extends CrudRepository<RedisChatRoom, String> {

    // redis 채팅방 찾기
    @Override
    Optional<RedisChatRoom> findById(String s);


    // 동작하지 않음! (NPE 발생) -> CrudRepository 관련해서 서치 필요해보임.
    // leave(String roomId, String sesionId) method on RedisChatRoomServiceImpl.class
    RedisChatRoom findByRoomId(String roomId);

    // 채팅방저장하기
    @Override
    <S extends RedisChatRoom> S save(S entity);


}
