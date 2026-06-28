package com.project.CrisisGrid.crisis_service.config;

import com.project.CrisisGrid.crisis_service.user.User;
import com.project.CrisisGrid.crisis_service.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepo userRepo;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepo.findByEmail(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found: " + username
                            ));

            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities("USER")
                    .build();
        };
    }
}