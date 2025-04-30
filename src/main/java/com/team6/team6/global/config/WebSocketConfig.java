package com.team6.team6.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
//@EnableWebSocketSecurity
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 구독 경로 프리픽스 설정
        registry.enableSimpleBroker("/topic","/queue");
        // 메시지 발행 경로 프리픽스 설정
        registry.setApplicationDestinationPrefixes("/app");
    }

//    @Bean
//    public AuthorizationManager<Message<?>> messageAuthorizationManager(
//            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
//        messages
//                .nullDestMatcher().permitAll()
//                .simpDestMatchers("/app/**").permitAll()
//                .simpSubscribeDestMatchers("/topic/**").permitAll()
//                .anyMessage().permitAll();
//        return messages.build();
//    }

}