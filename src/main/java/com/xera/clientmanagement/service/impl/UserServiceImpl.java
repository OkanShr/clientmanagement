package com.xera.clientmanagement.service.impl;

import com.xera.clientmanagement.repository.UserRepository;
import com.xera.clientmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public final UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                // Assume we are directly using email here
                return userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not Found"));
            }
        };
    }

}
