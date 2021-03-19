package com.springboot.dgumarket.repository.chat;

import com.springboot.dgumarket.dto.chat.ChatRoomTradeHistoryDto;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by MS (2020-12-28 8:22)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    // 유저와 관련된 모든 채팅방을 가져옵니다. 오직 Spring Data JPA 네임드 메서드
    List<ChatRoom> findChatRoomsByConsumerAndConsumerDeletedOrSellerAndSellerDeleted(Member consumer, int consumerDeleted, Member seller, int sellerDeleted);

    // 채팅방 존재유무 체크
    ChatRoom findChatRoomsByProductIdAndSellerIdAndConsumerId (int productId, int sellerId, int consumerId);

    @Query("SELECT r FROM ChatRoom r " +
            "WHERE (r.product.id = :productId and r.seller.id = :senderId and r.consumer.id = :receiverId) " +
            "OR (r.product.id = :productId and r.seller.id = :receiverId and r.consumer.id = :senderId)")
    ChatRoom findChatRoomPSR (
            @Param("productId")int productId,
            @Param("senderId")int senderId,
            @Param("receiverId")int receiverId
    );


    // 방번호 방 찾기
    @Override
    ChatRoom getOne(Integer integer);

    // 채팅방 저장하기
    @Override
    <S extends ChatRoom> S save(S s);

    // 물건의 채팅방 수
    long countChatRoomsByProduct(Product product);
}
