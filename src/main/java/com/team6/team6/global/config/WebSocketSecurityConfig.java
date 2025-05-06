package com.team6.team6.global.config;

import com.team6.team6.global.security.RoomAccessAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final ApplicationContext applicationContext;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(messageAuthorizationManager());
        AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(applicationContext);
        authz.setAuthorizationEventPublisher(publisher);
        // SecurityContextChannelInterceptor만 등록 (CSRF 인터셉터는 등록하지 않음)
        registration.interceptors(new SecurityContextChannelInterceptor());
        registration.interceptors(authz);
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
                new MessageMatcherDelegatingAuthorizationManager.Builder();

        messages
                // 특정 방에만 접근 가능하도록 커스텀 Authorization Manager 적용
                .simpSubscribeDestMatchers("/topic/room/{roomKey}/messages").access(
                        new RoomAccessAuthorizationManager()
                )
                .simpDestMatchers("/app/room/{roomKey}/keyword").access(new RoomAccessAuthorizationManager())

                // 다른 메시지는 인증된 사용자에게만 허용
                .anyMessage().authenticated();

        return messages.build();
    }
}