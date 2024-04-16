package com.akfnt.cnsedgeservice.user;

import java.util.List;

public record User(
        String username,
        String firstname,
        String lastname,
        List<String> roles
) {
}
