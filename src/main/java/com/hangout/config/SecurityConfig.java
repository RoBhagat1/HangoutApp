package com.hangout.config;

import com.hangout.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/h2-console/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/rsvps/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/drivers/**").permitAll()

                // Allow anyone to RSVP
                .requestMatchers(HttpMethod.POST, "/api/rsvps/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/drivers/*/join").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/drivers/*/leave").permitAll()

                // Protected endpoints (require authentication)
                .requestMatchers(HttpMethod.POST, "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/drivers").authenticated()

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
