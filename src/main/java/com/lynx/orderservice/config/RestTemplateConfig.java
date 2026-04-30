package com.lynx.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application-wide configuration for Spring Beans.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a singleton RestTemplate bean to be used for making synchronous
     * outbound HTTP requests. This bean will be managed by the Spring container
     * and injected into any component that requires it.
     *
     * @return A new instance of {@link RestTemplate}.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
