package com.ch4.lumia_backend.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    public RestTemplate openAiRestTemplate(RestTemplateBuilder builder, OpenAiProperties openAiProperties) {
        Duration timeout = Duration.ofMillis(Math.max(openAiProperties.getTimeoutMillis(), 1000));
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
