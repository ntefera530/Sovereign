package com.sovereign.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class PlaidConfig {

    @Value("${plaid.client-id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    @Value("${plaid.env}")
    private String env; // sandbox | development | production

    @Bean
    public PlaidApi plaidApi() {
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);

        ApiClient apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(resolveAdapter());
        
        return apiClient.createService(PlaidApi.class);
    }

    private String resolveAdapter() {
        return switch (env) {
            case "production" -> ApiClient.Production;
            //case "development" -> ApiClient.Development;
            default -> ApiClient.Sandbox;
        };
    }
    
}
