package com.akfnt.cnsedgeservice.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserController {
    @GetMapping("user")
    public Mono<User> getUser(
            @AuthenticationPrincipal OidcUser oidcUser      // 현재 인증된 사용자에 대한 정보를 가지고 있는 OidcUser 객체를 주입
    ) {
        if(oidcUser == null) {
            return Mono.empty();
        }

        var user = new User(                                // OidcUser에 있는 관련 클레임에서 사용자 객체 생성
                oidcUser.getPreferredUsername(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                oidcUser.getClaimAsStringList("roles")      // roles 클레임을 추출해 문자열 리스트로 가져온다
        );

        return Mono.just(user);
    }
}