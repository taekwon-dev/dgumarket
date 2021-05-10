package com.springboot.dgumarket.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Created by TK YOUN (2020-11-27 오전 9:06)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {

        messages
                .simpDestMatchers("/tk_testing/topic/**").permitAll();
    }



    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
