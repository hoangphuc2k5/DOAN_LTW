package com.stackoverflow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Admin and Manager areas (must be first)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                
                // User authenticated actions (must be before permitAll)
                // Questions & Answers
                .requestMatchers("/questions/ask", "/questions/*/edit", "/questions/*/delete").authenticated()
                .requestMatchers("/questions/*/images/*/delete").authenticated() // Delete images from questions
                .requestMatchers("/questions/*/upvote", "/questions/*/downvote").authenticated()
                .requestMatchers("/answers/create", "/answers/*/edit", "/answers/*/delete").authenticated()
                .requestMatchers("/answers/*/upvote", "/answers/*/downvote", "/answers/*/accept").authenticated()
                
                // Comments
                .requestMatchers("/questions/*/comments", "/answers/*/comments").authenticated()
                .requestMatchers("/comments/*/delete", "/comments/*/edit").authenticated()
                
                // Profile & User actions
                .requestMatchers("/profile", "/profile/edit", "/profile/update").authenticated()
                .requestMatchers("/profile/change-password").authenticated()
                
                // Messages (all require authentication)
                .requestMatchers("/messages/**").authenticated()
                
                // Notifications (all require authentication)
                .requestMatchers("/notifications/**").authenticated()
                
                // Follow system
                .requestMatchers("/follow/**").authenticated()
                
                // Reports
                .requestMatchers("/reports/**").authenticated()
                
                // User management - ADMIN only for edit/delete
                .requestMatchers("/users/*/edit", "/users/*/delete").hasRole("ADMIN")
                
                // Public pages (must be after authenticated matchers)
                .requestMatchers("/", "/home").permitAll()
                .requestMatchers("/questions", "/questions/*/view", "/questions/{id}").permitAll()
                .requestMatchers("/tags", "/tags/**").permitAll()
                .requestMatchers("/users", "/users/*").permitAll() // View only
                .requestMatchers("/profile/{username}").permitAll() // Public profile view
                
                // Auth pages
                .requestMatchers("/login", "/register", "/api/auth/**").permitAll()
                
                // Password reset
                .requestMatchers("/password/**").permitAll()
                
                // Public API endpoints
                .requestMatchers("/api/tags/**").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**").permitAll()
                .requestMatchers("/uploads/**", "/attachments/**").permitAll()
                
                // Error pages
                .requestMatchers("/error", "/error/**").permitAll()
                
                // WebSocket
                .requestMatchers("/ws/**", "/topic/**", "/queue/**").permitAll()
                
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error/403")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureHandler(authenticationFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "jwt")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}

