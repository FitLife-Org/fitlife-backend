package com.fitlife.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Initialize the RestTemplate tool and assign it to Spring Boot to manage (Bean)
    // From now on, any Service that needs to call an external API just needs @RequiredArgsConstructor to use it immediately.
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000); // Đợi AI trả lời trong 60s
        return new RestTemplate(factory);
    }
}