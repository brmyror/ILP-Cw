package uk.ac.ed.acp.cw2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

@Configuration
@EnableScheduling
public class IlpRestServiceConfig {
    @Value("${ilp.service.url}")
    private URL serviceUrl;

    @Bean
    public URL ilpServiceUrl() {
        return serviceUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
