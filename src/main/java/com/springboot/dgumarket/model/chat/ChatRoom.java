package com.springboot.dgumarket.model.chat;


import com.fasterxml.jackson.annotation.*;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.service.chat.ChatMessageServiceImpl;
import lombok.*;
import org.hibernate.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    public LocalDateTime getUsersEntranceDate(Member member){
        if (this.seller == member){  // 판매자일 경우
            return this.sellerEntranceDate;
        }else if (this.consumer == member){ // 구매자일 경우
            return this.consumerEntranceDate;
        }else {
            return null;
        }
    }

    // 내가 대화하고 있는 `상대방`을 가져옴
    public Member getMemberOpponent(Member member){
        if (this.seller == member){
            return this.consumer;
        }else{
            return this.seller;
        }
    }

    public boolean isMine(Member member){
        if(this.seller == member){ // 판매자일경우
            return true;
        }else{
            return false;
        }
    }

    // 채팅방 들어갈 떄 update entrance Date
    public void updateEntranceDate(int userId){
        LocalDateTime currentDateTime = LocalDateTime.now();
        if(this.getConsumer().getId() == userId){
            if(this.getConsumerDeleted()==1){ // 이미 나간상태라면(채팅으로 거래하기 시)
                this.setConsumerDeleted(0);
            }
            this.setConsumerEntranceDate(currentDateTime);
        }else {
            if(this.getSellerDeleted()==1){ // 이미 나간상태라면 (채팅으로 거래하기 시)
                this.setSellerDeleted(0);
            }
            this.setSellerEntranceDate(currentDateTime);
        }
    }

    // 채팅방 나간시간 가져오기
    public LocalDateTime getUserleaveDate(Member member){
        if(this.getConsumer() == member){
            return this.getConsumerDeletedDate();
        }else{
            return this.getSellerDeletedDate();
        }
    }

    // 채팅방 나가기(완전히)
    public void leave(int userId) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (this.getConsumer().getId() == userId){
            this.setConsumerDeletedDate(currentDateTime);
            this.setConsumerDeleted(1);
        }else if(this.getSeller().getId() == userId){
            this.setSellerDeletedDate(currentDateTime);
            this.setSellerDeleted(1);
        }
    }

    // 거래상태 거래완료로 바꾸기
    public void changeProductStatus(int status){
        this.getProduct().setTransactionStatusId(status);
    }

    // 유저초대하기 ( 상대방 나가기유무 0 -> X )
    public void changeExitToJoin(int receiverId){
        // 메시지 받는 유저가 채팅방 나간경우
        if(this.getConsumer().getId() == receiverId & this.getConsumerDeleted() == 1){
            logger.info("받는 이는 구매자입니다. 채팅방 나감 ? : {}", this.getConsumerDeleted());
            this.setConsumerDeleted(0);
        }else if(this.getSeller().getId() == receiverId & this.getSellerDeleted() == 1){
            logger.info("받는 이는 판매자입니다. 채팅방 나감 ? : {}",this.getSellerDeleted());
            this.setSellerDeleted(0);
        }else{
            logger.info("보내는상대아이디 : {}, consumerId : {}, sellerId : {}",receiverId, this.getConsumer().getId(), this.getSeller().getId());
            logger.info("이건 실행되서는 안되! ");
        }
    }

}