package com.akfnt.cnsedgeservice.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


// RequestRateLimiter 필터를 사용하면 Rate Limit 설정 값과 시간 윈도 내에 허용된 남은 요청 수 정보를 응답 헤더에 전송하게 되는데
// 이 정보들은 악용될 소지가 있기 때문에 응답 헤더에서 제거하기 위한 커스텀 필터를 정의
// Spring Cloud Gateway 에서 제공하는 RemoveResponseHeaderGatewayFilterFactory 도 있으나 정상적으로 동작하지 않아 새로 정의함
@Slf4j
@Component
public class RemoveMultipleResponseHeadersGatewayFilterFactory extends AbstractGatewayFilterFactory<RemoveMultipleResponseHeadersGatewayFilterFactory.Config> {
    public RemoveMultipleResponseHeadersGatewayFilterFactory() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.beforeCommit(() -> {
                List<String> headersToRemove = config.getHeadersToRemove();
                if(headersToRemove != null && !headersToRemove.isEmpty()) {
                    for(String header : headersToRemove) {
                        log.debug("[RequestRateLimiter Info] header name: {}, header values: {}", header, response.getHeaders().get(header));

                        response.getHeaders().remove(header);
                    }
                }

                return Mono.empty();
            });

            return chain.filter(exchange);
        });
    }

    public static class Config {
        private List<String> headersToRemove;

        public List<String> getHeadersToRemove() {
            return headersToRemove;
        }

        public void setHeadersToRemove(List<String> headersToRemove) {
            this.headersToRemove = headersToRemove;
        }
    }
}
