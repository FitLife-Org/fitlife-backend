package com.fitlife.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Initialize the RestTemplate tool and assign it to Spring Boot to manage (Bean)
    // From now on, any Service that needs to call an external API just needs @RequiredArgsConstructor to use it immediately.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}