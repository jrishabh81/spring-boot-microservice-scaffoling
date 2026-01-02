/* (C)2025 */
package com.rjain.spring_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenSearchConfig {

    @Bean
    public RestTemplate openSearchRestTemplate() {
        // Basic RestTemplate - connects to http://opensearch:9200
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // No proxy by default; adjust if needed
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        return new RestTemplate(requestFactory);
    }
}
