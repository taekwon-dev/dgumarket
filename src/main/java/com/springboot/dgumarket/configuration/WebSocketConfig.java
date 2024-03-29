package com.springboot.dgumarket.configuration;


import com.springboot.dgumarket.service.chat.RedisChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.*;

/**
 * Created by TK YOUN (2020-11-17 오후 8:13)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */


@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final RedisChatRoomService redisChatRoomService;

    public WebSocketConfig(@Lazy RedisChatRoomService redisChatRoomService){
        this.redisChatRoomService=redisChatRoomService;
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024); // default : 64 * 1024
        registration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
        registration.setSendBufferSizeLimit(3* 512 * 1024); // default : 512 * 1024
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setUserDestinationPrefix("/user");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // sockJS 클라이언트가 WebSocket Handshake를 하기 위해 연결할 EndPoint를 지정할 수 있다.
        // ws://localhost:8080/gs-guide-websocket/246/nvhffhfc/websocket
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
        // https://www.concretepage.com/spring-5/spring-websocket ; Registers error handler
//        registry.setErrorHandler(customErrorHandler);
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {
            /**
             * Invoked before the Message is actually sent to the channel.
             * This allows for modification of the Message if necessary.
             * If this method returns {@code null} then the actual
             * send invocation will not occur.
             *
             * @param message
             * @param channel
             */
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
                StompCommand command = sha.getCommand();

                if (StompCommand.CONNECT.equals(command)) {
                    log.info("{}, message.getPayload {}", command, message.getPayload());
                    log.info("{}, message.getHeader {}", command, message.getHeaders());
                    log.info("{}, accessor.toString {}", command, sha.toString());
                } else if(StompCommand.UNSUBSCRIBE.equals(command) && message.getHeaders().get("simpSubscriptionId").toString().startsWith("room-user-")) {
                    String sessionId = message.getHeaders().get("simpSessionId").toString(); // 유저 고유의 SessionId
                    redisChatRoomService.leave(sessionId); // 레디스 채팅방 나가기
                } else if(StompCommand.DISCONNECT.equals(command)) { // 갑작스러운 종료에도 채팅창을 잘 나가게 해야한다.
                    String sessionId = message.getHeaders().get("simpSessionId").toString();
                    log.info("[채팅][웹소켓네트워크] 사용자의 갑작스러운 종료감지! sessionId : {}", sessionId);
                    redisChatRoomService.leave(sessionId); // TODO : 갑작스러운 종료에도 sessionId로 채팅방 나갈 수 있도록 하기
                }
                return message;
            }
        });
    }

}
