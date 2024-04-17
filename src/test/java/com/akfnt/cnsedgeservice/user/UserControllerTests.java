package com.akfnt.cnsedgeservice.user;

import com.akfnt.cnsedgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTests {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenNotAuthenticatedThen401() {
        webTestClient
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedThenReturnUser() {
        var expectedUser = new User("jon.snow", "Jon", "Snow", List.of("employee", "customer"));

        webTestClient
                .mutateWith(configureMockOidcLogin(expectedUser))       // OIDC 에 기반해 인증 콘텍스트를 정의
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .value(user -> assertThat(user).isEqualTo(expectedUser));
    }

    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidcLogin(User expectedUser) {
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(
                builder -> {    // 모의 ID 토큰을 생성
                    builder.claim(StandardClaimNames.PREFERRED_USERNAME, expectedUser.username());
                    builder.claim(StandardClaimNames.GIVEN_NAME, expectedUser.firstname());
                    builder.claim(StandardClaimNames.FAMILY_NAME, expectedUser.lastname());
                });
    }
}
