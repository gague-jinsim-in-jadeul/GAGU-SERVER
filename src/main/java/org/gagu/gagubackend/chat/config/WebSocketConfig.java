package org.gagu.gagubackend.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker // 메세지 브로커가 지원하는 WebSocket 메세지 처리 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { // 메모리 기반 메세지 브로커
        config.enableSimpleBroker("/sub","/user");
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/chat-2d")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 클라이언트의 인바운드 채널에 대한 설정을 구성하는 메서드
     * interceptors() 메서드 사용 시 stompHandler 등록
     * 클라이언트의 WebSocket 연결 이전에 처리 작업을 수행 할 수 있도록 한다.
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
