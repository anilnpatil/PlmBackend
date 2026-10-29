package com.nextfirst.plm.auth.dto;

import java.util.Set;

public record RoleResponse(Long id, String code, String name, String description, Set<String> permissions) {}
