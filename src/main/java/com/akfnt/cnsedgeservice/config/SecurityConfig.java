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
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

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
                .build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");     // OIDC 공급자인 키클록에서 로그아웃 후 사용자를 스프링에서 동적으로 지정하는
                                                                            // 애플리케이션의 베이스 URL 로 리다이렉션한다 (로컬에서는 http://localhost:9000 이다)
        return oidcLogoutSuccessHandler;
    }
}