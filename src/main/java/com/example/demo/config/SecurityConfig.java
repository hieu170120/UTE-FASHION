package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.example.demo.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

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

	/**
	 * CharacterEncodingFilter bean để đảm bảo UTF-8 encoding
	 */
	@Bean
	public CharacterEncodingFilter characterEncodingFilter() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);
		return filter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf
					.ignoringRequestMatchers(
						"/api/**",                          // Bỏ CSRF cho tất cả API
						"/admin/promotions/toggle/**"       // Bỏ CSRF cho toggle endpoint
					)
				)

				.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/logout", "/forgot-password", "/reset-password",
								"/static/**", "/css/**", "/js/**", "/images/**", "/products", "/products/**", "/cart",
								"/cart/**", "/api/cart/**", "/checkout", "/checkout/**", "/payment", "/payment/**",
								"/payment-test", "/payment-test/**", "/api/auth/**", "/api/**", // Public product APIs
								"/verify-email", "/verify-email/**", "/resend-otp", "/error", // Allow error page
                                "/ws/**" //WebSocket
							)
						.permitAll().requestMatchers("/api/v1/reviews/**").authenticated() // Review APIs need auth
						// .requestMatchers("/admin/**").hasRole("ADMIN") // Admin only - COMMENTED FOR
						// TESTING
						// .requestMatchers("/shipper/**").hasRole("SHIPPER") // Shipper only -
						// COMMENTED FOR TESTING
						.requestMatchers("/profile", "/profile/**", "/orders", "/orders/**", "/vendor/**", "/admin/**", "/shipper/**")
						.authenticated().anyRequest().authenticated())
				.formLogin(form -> form.disable())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout=true")
						.invalidateHttpSession(true)
						.deleteCookies("UTE_FASHION_SESSION")
						.permitAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
