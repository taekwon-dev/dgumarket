package com.springboot.dgumarket.repository.chat;

import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import org.hibernate.annotations.OrderBy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Created by MS (2020-12-28 8:22)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // 가장 최근 메시지 가져오기
    ChatMessage findFirstByRoomIdOrderByIdDesc(Integer roomId);

    // 채팅방의 사용자(판매자 or 구매자)가 들어간 시점(채팅방에 있으니 조인을 통해 들고오던지 해야함, 아니면 파라미터로 받아서) 이후의 읽지 않은 메시지 가져오지
    Long countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(int roomId, LocalDateTime entranceDate, Member member, int msgStatus);
    Long countAllByRoomIdAndReceiverAndMsgStatus(int roomId, Member member, int msgStatus);

    // 특정 시점 이후의 메시지들만 가져온다. (ex, 채팅방 나가기한 유저에게 누군가 메시지를 보내면, 채팅방 들어갔을 떄 나가기한 시점 이후의 메시지들만 가져와야한다)
    List<ChatMessage> findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(int roomId, LocalDateTime dateTime);
    List<ChatMessage> findChatMessagesByRoomIdOrderByMsgDate(int roomId);

    // 메시지 저장
    @Override
    <S extends ChatMessage> S save(S s);


    /**
     * 2020 - 12 - 30
     * 유저가 읽지 않은 모든 메시지를 찾음
     * [USAGE]
     *      최초 클라이언트가 접속했을 때 현재 몇개의 읽지 않은 메시지가 있는 지 알려줌
     *      ex) [클라이언트] 채팅 플로팅버튼 위에 [읽지 않은 메시지 : 10개 ]
     * @param userId 유저 번호
     * @return 유저가 읽지 않은(status = 0) 모든 메시지들
     */
    @Query("select c from ChatMessage c where c.receiver.id = :userId and c.msgStatus = 0")
    List<ChatMessage> findUnReadMessagesByUserId(@Param("userId")Integer userId);




    // 채팅방의 모든 메시지를 가져옴
    List<ChatMessage> findChatMessagesByRoomIdOrderByMsgDateDesc(@Param("roomId")Integer roomId);


    // 특정룸의 entrance 기준 이후의 채팅메시지들
    @Query("select c from ChatMessage c where c.roomId= :roomId and c.receiver.id = :userId and c.msgStatus = 0 and c.msgDate > :entrance_date")
    List<ChatMessage> findChatMessagesByRoomIdAndReceiverAndMsgStatusAndMsgDateAfter();


    /**
     * 읽지 않은 메시지를 읽음 상태로 업데이트 한 후
     * 업데이트한 메시지 개수를 반환
     *
     * [USAGE]
     *      전제 :
     *          B가 A에게 채팅을 보낸 상태이며, B는 현재 채팅을 보낸 채팅방에 있는 상태
     *
     *      A가 채팅방에 들어가면
     *      B가 A에게 보낸 메시지를 읽지않음(status 0) 에서 읽음(status 1) 으로 바꾼다.          이떄
     *      메시지 상태를 읽지않음에서 읽음으로 상태를 바꾼 메시지의 '개수' 가 1개 이상인 경우
     *          B [클라이언트 UI] 에 '읽지않음' 메시지를 '읽음' 으로 바뀌게 하기 위해 B에게 메시지 전달
     *
     *
     * @param roomId 방 번호
     * @param userId 사용자 번호
     * @return 메시지 상태를 읽지않음에서 읽음으로 상태를 바꾼 메시지의 '개수'
     */
    @Modifying
    @Transactional
    @Query("update ChatMessage m set m.msgStatus = 1 where m.roomId = :roomId and m.receiver.id = :userId and m.msgStatus = 0")
    Integer updateReadstatus(@Param("roomId") Integer roomId, @Param("userId") Integer userId);


}
