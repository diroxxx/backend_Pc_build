package org.example.backend_pcbuild.configuration;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.config.UserAuthProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final UserAuthProvider userAuthProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Brak nagłówka Authorization lub niepoprawny format");
            }

            String token = authHeader.substring(7);

            Authentication authentication = userAuthProvider.validateToken(token);

            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("STOMP połączony jako: " + authentication.getName());
        }

        return message;
    }
}