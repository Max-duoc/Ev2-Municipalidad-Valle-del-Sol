package cl.municipalidad.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.reportes.url}")
    private String reportesUrl;

    @Value("${services.monitoreo.url}")
    private String monitoreoUrl;

    @Bean("reportesClient")
    public WebClient reportesClient() {
        return WebClient.builder()
                .baseUrl(reportesUrl)
                .build();
    }

    @Bean("monitoreoClient")
    public WebClient monitoreoClient() {
        return WebClient.builder()
                .baseUrl(monitoreoUrl)
                .build();
    }
}
