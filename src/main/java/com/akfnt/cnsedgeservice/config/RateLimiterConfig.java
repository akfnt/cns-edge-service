package com.akfnt.cnsedgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver keyResolver() {
        // RequestRateLimiter 필터는 KeyResolver 빈을 통해 요청에 대해 사용할 버킷을 결정한다
        // 스프링 시큐리티의 현재 인증된 사용자를 버킷으로 사용하도록 기본 설정되어 있다
        // 시큐리티를 추가할 때까지는 요청에 대한 사용률 제한은 상수 값("anonymous")을 반환함으로써 모든 요청이 동일한 버킷을 사용하도록 지정한다
        return exchange -> Mono.just("anonymous");
    }
}
