package com.akfnt.cnsedgeservice.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class UserController {
    @GetMapping("user")
    public Mono<User> getUser(
            @AuthenticationPrincipal OidcUser oidcUser      // 현재 인증된 사용자에 대한 정보를 가지고 있는 OidcUser 객체를 주입
    ) {
        if(oidcUser == null) {
            return Mono.empty();
        }

        var user = new User(                        // OidcUser에 있는 관련 클레임에서 사용자 객체 생성
                oidcUser.getPreferredUsername(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                List.of("employee", "customer")     // 임시적으로 하드코딩 값 지정
        );

        return Mono.just(user);
    }
}