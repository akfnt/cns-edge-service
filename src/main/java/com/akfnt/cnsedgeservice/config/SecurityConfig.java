package com.akfnt.cnsedgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    // SecurityWebFilterChain 빈은 애플리케이션에 대한 보안 정책을 정의하고 설정하는 데 사용한다
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico")
                            .permitAll()                                        // SPA 의 정적 리소스에 대한 인증되지 않은 액세스 허용
                        .pathMatchers(HttpMethod.GET, "/books/**")
                            .permitAll()                                        // 카탈로그의 도서에 대한 인증되지 않은 액세스 허용
                        .anyExchange().authenticated()                          // 그 외 다른 요청은 인증이 필요하다
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(
                                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)     // 인증되지 않은 사용자에 대해 HTTP 401로 응답한다
                        ))
                .oauth2Login(Customizer.withDefaults())                         // OAuth2 / OpenID 커넥트를 사용한 사용자 인증을 활성화한다
                .logout(logout -> logout.logoutSuccessHandler(
                        oidcLogoutSuccessHandler(clientRegistrationRepository)  // 로그아웃이 성공적으로 완료되는 경우에 대한 사용자 지정 핸들러를 정의
                ))
                .csrf((csrf) -> {                                               // 앵귤러 프런트엔드와 CSRF 토큰을 교환하기 위해 쿠키 기반 방식을 사용
                        // https://docs.spring.io/spring-security/reference/5.8/migration/reactive.html#_i_am_using_angularjs_or_another_javascript_framework
                        CookieServerCsrfTokenRepository tokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
                        XorServerCsrfTokenRequestAttributeHandler delegate = new XorServerCsrfTokenRequestAttributeHandler();
                        // Use only the handle() method of XorServerCsrfTokenRequestAttributeHandler and the
                        // default implementation of resolveCsrfTokenValue() from ServerCsrfTokenRequestHandler
                        ServerCsrfTokenRequestHandler requestHandler = delegate::handle;

                        csrf.csrfTokenRepository(tokenRepository).csrfTokenRequestHandler(requestHandler);
                })
                .build();
    }

    // CsrfToken 리액티브 스트림을 구독하고 이 토큰의 값을 올바르게 추출하기 위한 목적만을 갖는 필터
    // CookieServerCsrfTokenRepository 는 CsrfToken 구독을 보장하지 않으므로 WebFilter 안에서 이에 대한 해결 방안을 명시적으로 제공해야 한다
    // https://github.com/spring-projects/spring-security/issues/5766
    @Bean
    WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
            return csrfToken.doOnSuccess(token -> {
                /* Ensures the token is subscribed to. */
            }).then(chain.filter(exchange));
        };
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");     // OIDC 공급자인 키클록에서 로그아웃 후 사용자를 스프링에서 동적으로 지정하는
                                                                            // 애플리케이션의 베이스 URL 로 리다이렉션한다 (로컬에서는 http://localhost:9000 이다)
        return oidcLogoutSuccessHandler;
    }
}