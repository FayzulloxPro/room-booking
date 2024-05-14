package dev.fayzullokh.roombooking.config;

import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository authUserRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Bad Credentials"));
        return new CustomUserDetails(authUser);
    }

    public void save(User authUser) {
        authUserRepository.save(authUser);
    }
}
