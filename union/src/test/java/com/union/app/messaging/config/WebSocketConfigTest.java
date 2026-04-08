package com.union.app.messaging.config;

import com.union.app.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    @Mock
    private ChannelRegistration channelRegistration;

    private WebSocketConfig config;

    @BeforeEach
    void setUp() {
        config = new WebSocketConfig(jwtTokenProvider);
    }

    @Test
    void shouldConfigureMessageBroker() {
        config.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).enableSimpleBroker("/topic", "/queue");
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(messageBrokerRegistry).setUserDestinationPrefix("/user");
    }

    @Test
    void shouldRegisterStompEndpoint() {
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);

        config.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry).addEndpoint("/ws");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
        verify(endpointRegistration).withSockJS();
    }

    @Test
    void shouldSetUserWhenTokenIsValid() {
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

        config.configureClientInboundChannel(channelRegistration);

        verify(channelRegistration).interceptors(captor.capture());

        ChannelInterceptor interceptor = captor.getValue();

        String token = "valid-token";
        String phone = "123456789";

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getPhoneNumberFromToken(token)).thenReturn(phone);

        Message<?> message = buildConnectMessage(token);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);

        assertThat(accessor).isNotNull();
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo(phone);
    }

    @Test
    void shouldThrowExceptionWhenAuthorizationHeaderMissing() {
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

        config.configureClientInboundChannel(channelRegistration);
        verify(channelRegistration).interceptors(captor.capture());

        ChannelInterceptor interceptor = captor.getValue();

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() ->
                interceptor.preSend(message, mock(MessageChannel.class))
        ).isInstanceOf(MessagingException.class)
                .hasMessageContaining("Missing Authorization header");
    }

    @Test
    void shouldThrowExceptionWhenTokenInvalid() {
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);

        config.configureClientInboundChannel(channelRegistration);
        verify(channelRegistration).interceptors(captor.capture());

        ChannelInterceptor interceptor = captor.getValue();

        String token = "bad-token";

        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        Message<?> message = buildConnectMessage(token);

        assertThatThrownBy(() ->
                interceptor.preSend(message, mock(MessageChannel.class))
        ).isInstanceOf(MessagingException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    private Message<?> buildConnectMessage(String token) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + token);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}