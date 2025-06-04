package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost");
        server.setDescription("Local server");

        Contact contact = new Contact();
        contact.setName("SureCost Team");
        contact.setEmail("info@surecost.com");
        contact.setUrl("https://surecost.com");

        License license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
            .title("SureCost API")
            .version("1.0")
            .contact(contact)
            .description("API for managing pharmacy items")
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(server));
    }
} 