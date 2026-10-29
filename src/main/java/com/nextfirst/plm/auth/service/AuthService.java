package com.nextfirst.plm.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.dto.LoginResponse;
import com.nextfirst.plm.auth.dto.CurrentUserResponse;
import com.nextfirst.plm.auth.dto.UserResponse;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.exception.AuthResourceNotFoundException;
import com.nextfirst.plm.auth.repository.AuthUserRepository;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final AuthUserRepository userRepository;
    private final int tokenValidityMinutes;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            AuthUserRepository userRepository,
            @Value("${plm.auth.token-validity-minutes:60}") int tokenValidityMinutes) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.tokenValidityMinutes = tokenValidityMinutes;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        AuthUser user = requireUser(authentication.getName());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(tokenValidityMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("plm-backend")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("token_use", "access")
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, expiresAt, toResponse(user), permissions(user));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(Authentication authentication) {
        AuthUser user = requireUser(authentication.getName());
        return new CurrentUserResponse(toResponse(user), permissions(user));
    }

    private AuthUser requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(AuthUser::isActive)
                .orElseThrow(() -> new AuthResourceNotFoundException("Active user", email));
    }

    private UserResponse toResponse(AuthUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream().map(AuthRole::getCode).collect(Collectors.toCollection(TreeSet::new)));
    }

    private Set<String> permissions(AuthUser user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
