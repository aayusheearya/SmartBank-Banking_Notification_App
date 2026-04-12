package com.bank_notification.config;

import com.bank_notification.service.AdminDetailsService;
import com.bank_notification.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final AdminDetailsService adminDetailsService;
    private final RoleBasedAuthenticationSuccessHandler roleSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          AdminDetailsService adminDetailsService,
                          RoleBasedAuthenticationSuccessHandler roleSuccessHandler,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.adminDetailsService = adminDetailsService;
        this.roleSuccessHandler = roleSuccessHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Using BCrypt for secure banking password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * UNIFIED USER DETAILS SERVICE
     * This checks the Admin table first. If the user isn't an admin,
     * it falls back to the regular User table.
     */
    @Bean
    public UserDetailsService unifiedUserDetailsService() {
        return username -> {
            try {
                return adminDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                // If not found in Admin, try the User table
                return customUserDetailsService.loadUserByUsername(username);
            }
        };
    }

    /**
     * Explicitly defining the AuthenticationManager to avoid
     * "Global Authentication Manager not configured" errors.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(unifiedUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplified banking API access
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible paths - Added /about and /contact here
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/perform_login",
                                "/register",
                                "/about",
                                "/contact",
                                "/contact/send",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Role-based access control
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(roleSuccessHandler) // Redirects based on ROLE_ADMIN or ROLE_USER
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )
                // Modern JWT filter for stateless session support
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}