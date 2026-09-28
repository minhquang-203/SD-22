package org.example.templatejava6.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> STAFF_ROLES = Set.of("CHU", "QUAN_LY", "NHAN_VIEN");
    private static final String CUSTOMER_ROLE = "KHACH_HANG";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (token.isEmpty() || !jwtTokenProvider.validate(token)) {
                writeUnauthorized(response, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
                return;
            }

            Claims claims = jwtTokenProvider.parseClaims(token);
            String vaiTro = claims.get("vaiTro", String.class);
            Integer userId = parseUserId(claims.getSubject());
            if (vaiTro == null || vaiTro.isBlank() || userId == null) {
                writeUnauthorized(response, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
                return;
            }

            if (CUSTOMER_ROLE.equals(vaiTro)) {
                Optional<KhachHang> khach = khachHangRepository.findById(userId);
                if (khach.isEmpty() || !Boolean.TRUE.equals(khach.get().getTrangThai())) {
                    writeUnauthorized(response, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
                    return;
                }
            } else if (STAFF_ROLES.contains(vaiTro)) {
                Optional<NhanVien> nv = nhanVienRepository.findById(userId);
                if (nv.isEmpty() || !Boolean.TRUE.equals(nv.get().getTrangThai())) {
                    writeUnauthorized(response, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
                    return;
                }
            } else {
                writeUnauthorized(response, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
                return;
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro));
            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private static Integer parseUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(subject.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FAILED");
        body.put("code", "TOKEN_INVALID");
        body.put("message", message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
