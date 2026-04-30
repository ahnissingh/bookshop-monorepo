package com.bookshop.auth.service;


import com.bookshop.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
/**
 * Processes user authentication while maintaining security against user enumeration.


 * Note: Even if a UsernameNotFoundException is explicitly thrown, Spring Security's
 * internal mechanisms (via AbstractUserDetailsAuthenticationProvider) will
 * automatically intercept it and rethrow it as a BadCredentialsException.
 * This is a deliberate security feature designed to ensure that the client
 * receives an identical error response regardless of whether the username
 * exists or the password is incorrect. By masking the specific failure
 * reason, we prevent attackers from verifying the existence of valid
 * usernames through response analysis.


 * throws UsernameNotFoundException if the user is not found, which will be
 *         masked as BadCredentialsException by the security filter chain.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository
                .findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
    }
}