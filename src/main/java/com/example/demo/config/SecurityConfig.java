package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Cấu hình Spring Security với JWT Authentication
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // PasswordEncoder đã được định nghĩa trong PasswordConfig

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/logout",
                                "/forgot-password", "/reset-password",
                                "/static/**", "/css/**", "/js/**", "/images/**",
                                "/products", "/products/**",
                                "/cart", "/cart/**",
                                "/api/cart/**",
                                "/checkout", "/checkout/**",
                                "/payment", "/payment/**",
                                "/payment-test", "/payment-test/**",
                                "/api/auth/**",
                                "/verify-email", "/verify-email/**",
                                "/resend-otp",
                                "/api/**",
                                "/admin/**",  // TODO: TEMPORARY - Remove this line in production
                                "/shipper/**")  // TODO: TEMPORARY - Remove this line in production
                        .permitAll()
                        // .requestMatchers("/admin/**").hasRole("ADMIN") // Admin only - COMMENTED FOR TESTING
                        // .requestMatchers("/shipper/**").hasRole("SHIPPER") // Shipper only - COMMENTED FOR TESTING
                        .requestMatchers("/profile", "/profile/**", "/orders", "/orders/**", "/vendor/**")
                        .authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("UTE_FASHION_SESSION")
                        .permitAll());

        return http.build();
    }
}
