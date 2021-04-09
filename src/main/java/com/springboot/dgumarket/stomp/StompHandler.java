//package com.springboot.dgumarket.stomp;
//
//import com.springboot.dgumarket.repository.chat.RedisChatRoomRepository;
//import com.springboot.dgumarket.service.chat.RedisChatRoomService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//
//@Component
//public class StompHandler implements ChannelInterceptor {
//    private static Logger logger = LoggerFactory.getLogger(StompHandler.class);
//
//    @Autowired
//    RedisChatRoomService redisChatRoomService;
//
//    /**
//     * Invoked before the Message is actually sent to the channel.
//     * This allows for modification of the Message if necessary.
//     * If this method returns {@code null} then the actual
//     * send invocation will not occur.
//     *
//     * @param message
//     * @param channel
//     */
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
//        StompCommand command = sha.getCommand();
//        if (StompCommand.CONNECT.equals(command)){
//            logger.info("{}, message.getPayload {}", command, message.getPayload());
//            logger.info("{}, message.getHeader {}", command, message.getHeaders());
//            logger.info("{}, accessor.toString {}", command, sha.toString());
//            logger.info("{}, accessor.getDestination()", sha.getDestination());
//        }else if(StompCommand.UNSUBSCRIBE.equals(command) && message.getHeaders().get("simpSubscriptionId").toString().startsWith("room-user-")) {
//            String roomId = message.getHeaders().get("simpSubscriptionId").toString().split("-")[2]; // room-id 파싱
//            String sessionId = message.getHeaders().get("simpSessionId").toString(); // 유저 고유의 SessionId
//            redisChatRoomService.leave(roomId, sessionId); // 레디스 채팅방 나가기
//        }else if(StompCommand.DISCONNECT.equals(command)){ // 갑작스러운 종료에도 채팅창을 잘 나가게 해야한다.
//            logger.info("{}, message.getPayload {}", command, message.getPayload());
//            logger.info("{}, message.getHeader {}", command, message.getHeaders());
//            logger.info("{}, accessor.toString {}", command, sha.toString());
//            logger.info("{}, accessor.getDestination()", sha.getDestination());
//        }
//        return message;
//    }
//}
