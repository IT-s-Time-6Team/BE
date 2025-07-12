package com.team6.team6.global.config;

import com.team6.team6.global.log.AuthInfoLoggingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:[*],http://127.0.0.1:[*]}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(
                            Arrays.asList(allowedOrigins.split(","))
                    );
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setAllowCredentials(true);
                    return configuration;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/rooms/*/member").permitAll() // 회원가입/로그인은 허용
                        .requestMatchers(HttpMethod.POST, "/rooms").permitAll() // 방 생성은 허용
                        .requestMatchers(HttpMethod.GET, "/rooms/*").permitAll()  // 방 조회는 임시로 허용                        .requestMatchers(HttpMethod.PATCH, "/rooms/*/close").authenticated() // 방 종료는 인증 필요
                        .requestMatchers("/docs/**").permitAll() // API 문서 접근 허용
                        .requestMatchers("/actuator/**").permitAll() // actuator 접근 허용
                        .anyRequest().authenticated()
                )
                // Spring Security 6부터는 인증 정보의 세션 저장 방식이 "자동 저장"에서 "명시적 저장"으로 변경됨
                .securityContext(context -> context
                        .requireExplicitSave(false) // 세션 자동 저장 활성화
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(new AuthInfoLoggingFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
