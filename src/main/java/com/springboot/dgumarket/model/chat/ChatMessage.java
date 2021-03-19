package com.springboot.dgumarket.model.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Builder
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    // 보내는 유저 아이디
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Member sender;

    // 받을 유저 아이디
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Member receiver;

    // 방번호
    @Column(name = "room_id")
    private int roomId;

    // 채팅방과 관련되어 있는 물품
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Product.class)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    // 메시지내용
    @Column(name = "message")
    private String message;

    // 메시지타입 0:텍스트, 1:이미지
    @Column(name = "message_type", columnDefinition = "TINYINT", length=1)
    private int msgType;

    // 메시지읽음상태 0:읽지않음, 1:읽음
    @Column(name = "message_status", columnDefinition = "TINYINT", length=1)
    private int msgStatus;

    @CreationTimestamp
    @Column(name = "message_datetime", updatable = true)
    private LocalDateTime msgDate;
}
