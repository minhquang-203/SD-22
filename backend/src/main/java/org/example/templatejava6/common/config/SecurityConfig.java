package org.example.templatejava6.common.config;

import org.example.templatejava6.common.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Value("${app.security.enforce-role-checks:true}")
    private boolean enforceRoleChecks;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (enforceRoleChecks) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/khach/**").permitAll()
                    .requestMatchers("/api/khach/quiz/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/nhan-vien/dang-nhap").permitAll()
                    .requestMatchers(HttpMethod.GET,
                            "/api/san-pham/**",
                            "/api/danh-muc/**",
                            "/api/thuong-hieu/**",
                            "/api/dang-san-pham/**",
                            "/api/mau-sac/**",
                            "/api/cong-dung/**",
                            "/api/thanh-phan/**",
                            "/api/chi-tiet-san-pham/**",
                            "/api/danh-gia/**",
                            "/api/phuong-thuc-thanh-toan/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/payments/vnpay/callback").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/hoa-don/tra-cuu").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/shipping/provinces", "/api/shipping/districts", "/api/shipping/wards").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/shipping/fee").permitAll()
                    .requestMatchers("/api/chat/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/api/khach-hang/toi", "/api/khach-hang/toi/**").hasRole("KHACH_HANG")
                    .requestMatchers("/api/yeu-thich/**").hasRole("KHACH_HANG")
                    .requestMatchers("/api/gio-hang", "/api/gio-hang/**").hasRole("KHACH_HANG")
                    .requestMatchers("/api/online", "/api/online/**").hasRole("KHACH_HANG")
                    .requestMatchers("/api/hoa-don/cua-toi", "/api/hoa-don/cua-toi/**").hasRole("KHACH_HANG")
                    .requestMatchers("/api/nhan-vien", "/api/nhan-vien/**").hasAnyRole("QUAN_LY", "CHU")
                    .requestMatchers("/api/**").hasAnyRole("NHAN_VIEN", "QUAN_LY", "CHU")
                    .anyRequest().permitAll()
            );
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }
}
