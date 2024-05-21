package org.gagu.gagubackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GaguBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GaguBackendApplication.class, args);
    }

}
