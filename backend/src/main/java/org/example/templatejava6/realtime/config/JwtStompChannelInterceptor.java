package org.example.templatejava6.realtime.config;

import org.example.templatejava6.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final Set<String> ADMIN_ROLES = Set.of("NHAN_VIEN", "QUAN_LY", "CHU");

    private final JwtTokenProvider jwtTokenProvider;

    public JwtStompChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication auth = authenticate(accessor);
            if (auth == null) {
                throw new IllegalArgumentException("WebSocket yêu cầu JWT hợp lệ.");
            }
            accessor.setUser(auth);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication auth = resolveAuth(accessor);
            if (auth == null) {
                throw new IllegalArgumentException("Chưa xác thực WebSocket.");
            }
            String destination = accessor.getDestination();
            if (!canSubscribe(auth, destination)) {
                throw new IllegalArgumentException("Không có quyền subscribe: " + destination);
            }
        }

        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String header = firstHeader(accessor, "Authorization");
        if (header == null || header.isBlank()) {
            header = firstHeader(accessor, "authorization");
        }
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        if (!jwtTokenProvider.validate(token)) {
            return null;
        }
        Claims claims = jwtTokenProvider.parseClaims(token);
        String vaiTro = claims.get("vaiTro", String.class);
        if (vaiTro == null || vaiTro.isBlank()) {
            return null;
        }
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro));
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    }

    private Authentication resolveAuth(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication) {
            return authentication;
        }
        return authenticate(accessor);
    }

    private boolean canSubscribe(Authentication auth, String destination) {
        if (destination == null || destination.isBlank()) {
            return false;
        }
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("");

        if (destination.startsWith("/topic/admin/")) {
            return ADMIN_ROLES.contains(role);
        }
        if (destination.startsWith("/topic/customers/")) {
            if (!"KHACH_HANG".equals(role)) {
                return false;
            }
            String prefix = "/topic/customers/";
            String rest = destination.substring(prefix.length());
            int slash = rest.indexOf('/');
            String customerId = slash >= 0 ? rest.substring(0, slash) : rest;
            return customerId.equals(String.valueOf(auth.getName()));
        }
        return false;
    }

    private static String firstHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
