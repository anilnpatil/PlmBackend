package com.nextfirst.plm.auth.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nextfirst.plm.auth.repository.AuthUserRepository;

@Service
public class PlmUserDetailsService implements UserDetailsService {
    private final AuthUserRepository userRepository;

    public PlmUserDetailsService(AuthUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .disabled(!user.isActive())
                        .authorities("PLM_USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
}
