package com.akfnt.cnsedgeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;
import static reactor.core.publisher.Mono.when;

@WebFluxTest
@Import(SecurityConfig.class)
public class SecurityConfigTests {
    @Autowired
    WebTestClient webTestClient;
    
    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenLogoutNotAuthenticatedAndNoCsrfTokenThen403() {
        webTestClient
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenLogoutAuthenticatedAndNoCsrfTokenThen403() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenLogoutAuthenticatedAnsWithCsrfTokenThen302() {
        given(clientRegistrationRepository.findByRegistrationId("test"))
                .willReturn(Mono.just(testClientRegistration()));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())          // 사용자 인증을 위한 모의 ID 토큰 사용
                .mutateWith(SecurityMockServerConfigurers.csrf())                   // 요청에 CSRF 토큰을 추가
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isFound();                                          // 로그아웃을 키클록으로 전파하기 위한 302 리다이렉션이 응답되어야 함
    }

    private ClientRegistration testClientRegistration() {
        // 키클록에 연결할 URL 을 얻기 위해 스프링 보안이 사용하는 ClientRegistration 모의 객체
        return ClientRegistration.withRegistrationId("test")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test")
                .authorizationUri("https://sso.polarbookshop.com/auth")
                .tokenUri("https://sso.polarbookshop.com/token")
                .redirectUri("https://polarbookshop.com")
                .build();
    }
}
