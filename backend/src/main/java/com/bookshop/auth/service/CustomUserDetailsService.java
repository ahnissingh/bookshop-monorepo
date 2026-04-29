package com.bookshop.auth.service;


import com.bookshop.auth.repository.UserRepository;
import com.bookshop.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UserNotFoundException {
        return userRepository
                .findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + usernameOrEmail));
    }
}