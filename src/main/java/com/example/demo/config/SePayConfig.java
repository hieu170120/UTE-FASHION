package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * SePayConfig
 * Configuration class for SePay payment gateway integration
 * Provides RestTemplate bean and exposes SePay properties
 */
@Configuration
public class SePayConfig {

    @Value("${sepay.api.url}")
    private String apiUrl;

    @Value("${sepay.api.key}")
    private String apiKey;

    @Value("${sepay.bank.account}")
    private String bankAccount;

    @Value("${sepay.bank.name}")
    private String bankName;

    @Value("${sepay.polling.interval}")
    private Long pollingInterval;

    @Value("${sepay.polling.timeout}")
    private Long pollingTimeout;

    /**
     * RestTemplate bean for HTTP calls to SePay API
     * Used by SePayServiceImpl
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Getters for accessing config values
    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public Long getPollingInterval() {
        return pollingInterval;
    }

    public Long getPollingTimeout() {
        return pollingTimeout;
    }
}
