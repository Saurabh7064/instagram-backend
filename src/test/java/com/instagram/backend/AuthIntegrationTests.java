package com.instagram.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("instagram")
            .withUsername("instagram")
            .withPassword("instagram");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-at-least-32-bytes");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName":"Integration User",
                                  "username":"integration.user",
                                  "email":"integration.user@example.com",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integration.user"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNumber());
    }

    @Test
    void loginReturnsJwtTokenForDemoUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "identifier":"demo.user",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo.user"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNumber());
    }

    @Test
    void meRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedUserWhenTokenProvided() throws Exception {
        String accessToken = loginAndExtractToken();

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo.user"))
                .andExpect(jsonPath("$.fullName").value("Demo User"))
                .andExpect(jsonPath("$.email").value("demo.user@example.com"))
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void meRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndExtractToken() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier":"demo.user",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return extractJsonValue(login.getResponse().getContentAsString(), "accessToken");
    }

    private String extractJsonValue(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Field not found in JSON response: " + fieldName);
        }
        return matcher.group(1);
    }
}
