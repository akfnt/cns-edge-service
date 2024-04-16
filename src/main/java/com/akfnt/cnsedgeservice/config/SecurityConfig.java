package com.akfnt.cnsedgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {
    // SecurityWebFilterChain 빈은 애플리케이션에 대한 보안 정책을 정의하고 설정하는 데 사용한다
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(exchange ->
                exchange.anyExchange().authenticated())     // 모든 요청은 인증이 필요하다
                .formLogin(Customizer.withDefaults())       // 사용자 인증을 로그인 양식을 통해 활성화한다.
                                                            // 기본 설정에는 프레임워크에서 제공하는 로그인 페이지와 인증이 되지 않은 경우 해당 페이지로의 자동 라다이렉션이 포함되어 있다
                .build();
    }
}