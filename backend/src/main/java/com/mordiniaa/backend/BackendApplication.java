package com.mordiniaa.backend;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
        info = @Info(
                title = "Teamwork REST API Documentation",
                description = "Documentation Of The Monolith Version Of The Teamwork App",
                contact = @Contact(
                        //This Is Fake Data
                        name = "Mordiniaa",
                        email = "portfolio-project@example.com",
                        url = "https://www.portfolio-project-mordiniaa.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Complex Documentation Of This Project",
                url = "https://www.portfolio-project-mordiniaa.com/team-work/swagger-ui/index.html"
        )
)
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mordiniaa.backend.repositories.mysql")
@EnableMongoRepositories(basePackages = "com.mordiniaa.backend.repositories.mongo")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}

