package org.inharidge.fairact_contract_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청 인증 없이 허용
                )
                .oauth2Login(oauth2 -> oauth2.disable()) // OAuth2 로그인 비활성화
                .csrf(csrf -> csrf.disable()); // 필요하면 CSRF 비활성화

        return http.build();
    }
}
