package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OpenAPIConfigTest {

    @Autowired
    private OpenAPIConfig openAPIConfig;

    @Test
    void testMyOpenAPI() {
        OpenAPI openAPI = openAPIConfig.myOpenAPI();
        
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("SureCost API", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());
        assertEquals("API for managing pharmacy items", openAPI.getInfo().getDescription());
        
        // Test contact information
        Contact contact = openAPI.getInfo().getContact();
        assertNotNull(contact);
        assertEquals("SureCost Team", contact.getName());
        assertEquals("info@surecost.com", contact.getEmail());
        assertEquals("https://surecost.com", contact.getUrl());
        
        // Test license information
        License license = openAPI.getInfo().getLicense();
        assertNotNull(license);
        assertEquals("MIT License", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());
        
        // Test servers
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost", server.getUrl());
        assertEquals("Local server", server.getDescription());
    }
} 