package com.akfnt.cnsedgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver keyResolver() {
        // RequestRateLimiter 필터는 KeyResolver 빈을 통해 요청에 대해 사용할 버킷을 결정한다
        return exchange -> exchange.getPrincipal()      // 현재 인증된 사용자(principal)를 현재 요청(exchange)에서 가져온다
                .map(Principal::getName)
                .defaultIfEmpty("anonymous");   // 요청이 인증되지 않았다면 사용률 제한을 적용하기 위한 기본 키 값으로 "anonymous"를 사용한다
    }
}
