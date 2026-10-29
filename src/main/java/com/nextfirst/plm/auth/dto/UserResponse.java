package com.nextfirst.plm.auth.dto;

import java.util.Set;

public record UserResponse(Long id, String email, String fullName, boolean active, Set<String> roles) {}
