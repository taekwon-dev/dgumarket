package com.springboot.dgumarket.payload.request.chat;


import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.sound.midi.Receiver;
import java.time.LocalDateTime;

// 보내는 메시지 형태
@Getter
@ToString
@Builder
public class SendMessage {
    private int productId;
    private int senderId;
    private int receiverId;
    private int messageType;
    private String message;

    public ChatMessage toEntityWith(int status, int roomId, Product product, Member receiver, Member sender){
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("메시지 저장시 localDateTime : " + localDateTime.toString());


        return ChatMessage.builder()
                .msgType(this.messageType)
                .message(this.message)
                .roomId(roomId) // 주입
                .product(product) // 주입
                .msgStatus(status) // 주입
                .sender(sender) // 주입
                .msgDate(localDateTime)
                .receiver(receiver).build(); // 주입
    }
}
