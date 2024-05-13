package dev.fayzullokh.roombooking.config;

import dev.fayzullokh.roombooking.entities.User;
import dev.fayzullokh.roombooking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// SecurityConfig
@Configuration
@RequiredArgsConstructor
public class SecurityConfig  {
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.
                cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/swagger-ui.html",
                        "/swagger-ui*/**",
                        "/swagger-ui*/*swagger-initializer.js",
                        "/v3/api-docs*/**",
                        "/error",
                        "/webjars/**",
                        "/api/open",
                        "/api/auth/**",
                        "/api/url/get/**"

                        /*,
                        "/**" // only for test*/
                )
                .permitAll()
                .anyRequest()
                .fullyAuthenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .addFilterBefore(new JwtTokenFilter(jwtTokenUtil, userDetailsService()), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User customer = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return new UserDetails(customer);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration source = new CorsConfiguration();
        source.setAllowedOriginPatterns(List.of("*"));
        source.setAllowedHeaders(List.of("*"));
        source.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source1 = new UrlBasedCorsConfigurationSource();
        source1.registerCorsConfiguration("/**", source);
        return source1;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }
}
