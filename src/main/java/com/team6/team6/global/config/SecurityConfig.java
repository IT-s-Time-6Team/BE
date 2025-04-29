package com.team6.team6.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/rooms/*/member").permitAll() // 회원가입/로그인은 허용
                        .requestMatchers(HttpMethod.POST, "/rooms").permitAll() // 방 생성은 허용
                        .requestMatchers(HttpMethod.GET, "/rooms/*").authenticated() // 방 조회는 인증 필요
                        .requestMatchers(HttpMethod.PATCH, "/rooms/*/close").authenticated() // 방 종료는 인증 필요
                        .anyRequest().authenticated()
                )
                // Spring Security 6부터는 인증 정보의 세션 저장 방식이 "자동 저장"에서 "명시적 저장"으로 변경됨
                .securityContext(context -> context
                        .requireExplicitSave(false) // 세션 자동 저장 활성화
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}