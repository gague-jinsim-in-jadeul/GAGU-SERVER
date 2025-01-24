package org.gagu.gagubackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
@OpenAPIDefinition(servers = {@Server(url = "https://gagu.me", description = "Default Server url")}
)
@SpringBootApplication
@EnableJpaAuditing
public class GaguBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GaguBackendApplication.class, args);
    }
}
