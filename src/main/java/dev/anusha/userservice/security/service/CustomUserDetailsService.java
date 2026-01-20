package dev.anusha.userservice.security.service;

import dev.anusha.userservice.model.User;
import dev.anusha.userservice.repository.UserRepository;
import dev.anusha.userservice.security.model.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if(userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User with username: "+username+" not found");
        }

        User user = userOptional.get();
        return new CustomUserDetails(user);
    }
}
