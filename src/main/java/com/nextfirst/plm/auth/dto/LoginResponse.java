package com.nextfirst.plm.auth.dto;

import java.time.Instant;
import java.util.Set;

public record LoginResponse(String accessToken, Instant expiresAt, UserResponse user, Set<String> permissions) {}
