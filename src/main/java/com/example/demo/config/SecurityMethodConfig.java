package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration for enabling method-level security
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityMethodConfig {
}