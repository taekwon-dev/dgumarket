package com.springboot.dgumarket.model.chat;


import com.fasterxml.jackson.annotation.*;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;

import lombok.*;
import org.hibernate.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;



@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@ToString
@Table(name = "chat_room")
@DynamicUpdate
@Entity
public class ChatRoom {
    private static Logger logger = LoggerFactory.getLogger(ChatRoom.class);

    // 방 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int roomId;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private Member seller;

    // 소비자
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "consumer_id", referencedColumnName = "id")
    private Member consumer;

    // 채팅방과 관련되어 있는 물품
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Product.class)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Product product;

    // 구매자 채팅방 삭제 유무
    @Column(name = "consumer_deleted")
    private int consumerDeleted;

    // 판매자 채팅방 삭제 유무
    @Column(name = "seller_deleted")
    private int sellerDeleted;

    // 구매자 채팅방 삭제 시간
    @Column(name = "consumer_deleted_date", updatable = true)
    private LocalDateTime consumerDeletedDate;

    // 판매자 채팅방 삭제 시간
    @Column(name = "seller_deleted_date", updatable = true)
    private LocalDateTime sellerDeletedDate;

    // 채팅방 생성 시간
    @CreationTimestamp
    @Column(name = "created_datetime", updatable = false)
    private LocalDateTime created;

    // 구매자 입장 시간
    @CreationTimestamp
    @Column(name = "consumer_entrance_date", updatable = true)
    private LocalDateTime consumerEntranceDate;

    // 판매자 입장 시간
    @Column(name = "seller_entrance_date", updatable = true)
    private LocalDateTime sellerEntranceDate;


    public LocalDateTime getUsersEntranceDate(Member loginUser) {

        // 채팅 상대방이
        // 판매자, 구매자가 각각 NULL 일 수 있다. -> NPE 예외 처리

        // ~NULL 조건 추가.
        if ((this.seller != null) && (this.seller == loginUser)) {
            // 로그인 유저가 판매자인 경우 -> 채팅 상대방 = 구매자
            return this.sellerEntranceDate;
        } else if ((this.consumer != null) && (this.consumer == loginUser)) {
            // 로그인 유저가 구매자인 경우 -> 채팅 상대방 = 판매자
            return this.consumerEntranceDate;
        } else {
            // 조건에 부합하지 않은 경우 NULL 반환해서 채팅 상대방이 NULL인 것으로 처리
            /** null 이면 상관 없어? (로직 전개) */
            return null;
        }
    }

    /** Report 관련해서 사용되는 부분은 체크하고, 나머진 체크 완료 */
    // 내가 대화하고 있는 `상대방`을 가져옴
    public Member getMemberOpponent(Member loginUser) {

        // 채팅 상대방이
        // 판매자, 구매자가 각각 NULL 일 수 있다. -> NPE 예외 처리

        // ~NULL 조건 추가.
        if ((this.seller != null) && (this.seller == loginUser)) {
            // 로그인 유저가 판매자인 경우 -> 채팅 상대방 = 구매자
            return this.consumer;
        } else if ((this.consumer != null) && (this.consumer == loginUser)) {
            // 로그인 유저가 구매자인 경우 -> 채팅 상대방 = 판매자
            return this.seller;
        } else {
            // 조건에 부합하지 않은 경우 NULL 반환해서 채팅 상대방이 NULL인 것으로 처리
            return null;
        }
    }

    /** this.seller -> NULL 이면? */
    public boolean isMine(Member member){
        if (this.seller == member) {
            return true;
        }else{
            return false;
        }
    }

    // 채팅방 들어갈 떄 update entrance Date
    /** this.getConsumer(), this.getSeller() -> NULL 이면? */
    public void updateEntranceDate(int userId){
        LocalDateTime currentDateTime = LocalDateTime.now();
        if(this.getConsumer().getId() == userId){
            this.setConsumerEntranceDate(currentDateTime);
        }else if(this.getSeller().getId() == userId){
            this.setSellerEntranceDate(currentDateTime);
        }
    }


    // 채팅방 나가기 상태에서 메시지를 하나 보냈을 시점에 만약 내가 나감상태라는 것은 채팅으로 거래하기를 통해 해당 화면에 들어왔을 경우 뿐이다.
    /** this.getConsumer(), this.getSeller() -> NULL 이면? */
    public void leave2enterForFirstMessage(Member member, LocalDateTime now){
        if(this.getConsumer() == member && this.getConsumerDeleted() == 1){ // 소비자면 소비자영역에 바꾸어줌
            this.setConsumerDeleted(0); // 나감유무 1에서0으로 바꿈
            this.setConsumerEntranceDate(now); // 입장일 갱신
        }else if(this.getSeller() == member && this.getSellerDeleted() == 1){ // 판매자면 판매자의 영역에서 바꾸어줌
            this.setSellerDeleted(0); // 나감유무 1에서0으로 바꿈
            this.setSellerEntranceDate(now); // 입장일 갱신
        }
    }

    // 채팅방 나간시간 가져오기
    public LocalDateTime getUserleaveDate(Member member){
        /** this.getConsumer() -> NULL 이면? */
        if(this.getConsumer() == member){
            return this.getConsumerDeletedDate();
        }else{
            return this.getSellerDeletedDate();
        }
    }

    // 채팅방 나가기(완전히)
    /** this.getConsumer(), this.getSeller() -> NULL 이면? */
    public void leave(int userId) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        // 채팅방 나가기 클릭 시점, (= 채팅방 삭제 시점)
        // 상대 유저가 탈퇴한 경우, NPE 발생하므로 NPE 체크
        // 로그인 유저에 대한 NPE 체크는 불필요 -> 이미 Gateway에서 필터링

        // 상대 유저가 구매자 또는 판매자인 경우, 각각의 경우 NULL일 경우 대비하기 위해
        // 실제 처리 코드 조건에 ~NULL 조건을 추가.

        // Hibernate: update chat_room set seller_deleted=?, seller_deleted_date=? where id=?

        if ((this.getConsumer() != null) && (this.getConsumer().getId() == userId)) {
            this.setConsumerDeletedDate(currentDateTime);
            this.setConsumerDeleted(1);

        } else if ((this.getSeller() != null) && (this.getSeller().getId() == userId)) {
            this.setSellerDeletedDate(currentDateTime);
            this.setSellerDeleted(1);
        }

        /** 이 외 조건은 상관 없어? */
    }

    // 유저초대하기 ( 상대방 나가기유무 0 -> X )
    /** this.getConsumer(), this.getSeller() -> NULL 이면? */
    public void changeExitToJoin(int receiverId){
        // 메시지 받는 유저가 채팅방 나간경우
        if (this.getConsumer().getId() == receiverId & this.getConsumerDeleted() == 1) {
            this.setConsumerDeleted(0);
        } else if (this.getSeller().getId() == receiverId & this.getSellerDeleted() == 1) {
            this.setSellerDeleted(0);
        } else {
            /** ? */
        }
    }

    // -------------------------------------- 채팅 - 유저  --------------------------------------------------- //

    // 채팅방 -> 유저 (판매자 또는 구매자) 방향으로 참조 관계 끊기 (setter 역할 대신)

    public void disconnChatRoomToConsumer() {
        this.consumer = null;
    }

    public void disconnChatRoomToSeller() {
        this.consumer = null;
    }

    public void disconnChatRoomToSellerProduct() {
        this.product = null;
    }
}