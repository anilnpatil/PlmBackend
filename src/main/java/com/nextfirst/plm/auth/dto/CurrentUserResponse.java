package com.nextfirst.plm.auth.dto;

import java.util.Set;

public record CurrentUserResponse(UserResponse user, Set<String> permissions) {}
