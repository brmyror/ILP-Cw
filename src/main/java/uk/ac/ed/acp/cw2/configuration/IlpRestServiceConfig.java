package uk.ac.ed.acp.cw2.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class IlpRestServiceConfig {

    // Default ILP endpoint to use when no property or env var is set.
    private static final String DEFAULT_ILP_ENDPOINT = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net";

    // Expose the resolved ILP endpoint as a bean so other components can inject it.
    @Bean(name = "ilpEndpoint")
    public String ilpEndpoint(Environment env) {
        // Prefer explicit Spring property first (allows overriding via application.yml/properties)
        String url = env.getProperty("ilp.service.url");
        if (url != null && !url.isBlank()) {
            return normalize(url);
        }

        // Then check environment variable ILP_ENDPOINT
        url = System.getenv("ILP_ENDPOINT");
        if (url != null && !url.isBlank()) {
            return normalize(url);
        }

        // Fallback to default
        return normalize(DEFAULT_ILP_ENDPOINT);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String normalize(String url) {
        if (url == null) return null;
        if (url.endsWith("/")) return url.substring(0, url.length() - 1);
        return url;
    }
}
