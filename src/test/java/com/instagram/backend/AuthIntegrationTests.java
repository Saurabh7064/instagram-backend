package com.instagram.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .andExpect(jsonPath("$.expiresAt").isNumber())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshExpiresAt").isNumber());
    }

    @Test
    void loginReturnsJwtTokenForDemoUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "identifier":"a",
                                  "password":"a"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("a"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNumber())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshExpiresAt").isNumber());
    }

    @Test
    void meRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedUserWhenTokenProvided() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("a"))
                .andExpect(jsonPath("$.fullName").value("A"))
                .andExpect(jsonPath("$.email").value("a@example.com"))
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void meRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsNewTokenPair() throws Exception {
        String refreshToken = loginAndExtract("refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken":"%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNumber())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshExpiresAt").isNumber());
    }

    @Test
    void refreshedAccessTokenCanAccessMe() throws Exception {
        String refreshToken = loginAndExtract("refreshToken");
        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken":"%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = extractJsonValue(refreshed.getResponse().getContentAsString(), "accessToken");

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("a"));
    }

    @Test
    void refreshRejectsAccessToken() throws Exception {
        String accessToken = loginAndExtract("accessToken");
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken":"%s"
                                }
                                """.formatted(accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profilePageRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profilePageReturnsBasicProfileData() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(get("/api/profile/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("a"))
                .andExpect(jsonPath("$.fullName").value("A"))
                .andExpect(jsonPath("$.bio").isString())
                .andExpect(jsonPath("$.postsCount").isNumber())
                .andExpect(jsonPath("$.followersCount").isNumber())
                .andExpect(jsonPath("$.followingCount").isNumber())
                .andExpect(jsonPath("$.joinedAt").isString());
    }

    @Test
    void feedReturnsSeededPostsForAuthenticatedUser() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(get("/api/feed")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").isString())
                .andExpect(jsonPath("$[0].caption").isString())
                .andExpect(jsonPath("$[0].imageUrl").isString());
    }

    @Test
    void createPostAddsNewFeedItem() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "caption":"Integration test post",
                                  "imageUrl":"/mock/post-canyon.svg",
                                  "locationLabel":"Integration Test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("a"))
                .andExpect(jsonPath("$.caption").value("Integration test post"))
                .andExpect(jsonPath("$.imageUrl").value("/mock/post-canyon.svg"));
    }

    @Test
    void likePostMarksFeedItemAsLiked() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(post("/api/posts/1/like")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.likedByViewer").value(true))
                .andExpect(jsonPath("$.likeCount").isNumber());
    }

    @Test
    void unlikePostRemovesViewerLike() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(post("/api/posts/1/like")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/posts/1/like")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.likedByViewer").value(false))
                .andExpect(jsonPath("$.likeCount").isNumber());
    }

    @Test
    void updateOwnPostChangesCaption() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        MvcResult created = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "caption":"Post to edit",
                                  "imageUrl":"/mock/post-canyon.svg",
                                  "locationLabel":"Edit Test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = extractNumericJsonValue(created.getResponse().getContentAsString(), "id");

        mockMvc.perform(put("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "caption":"Edited integration caption",
                                  "locationLabel":"Edited Location"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caption").value("Edited integration caption"))
                .andExpect(jsonPath("$.locationLabel").value("Edited Location"));
    }

    @Test
    void updateRejectsPostsOwnedByAnotherUser() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(put("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "caption":"Nope",
                                  "locationLabel":"Nope"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOwnPostRemovesCreatedPost() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        MvcResult created = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "caption":"Post to delete",
                                  "imageUrl":"/mock/post-canyon.svg",
                                  "locationLabel":"Delete Test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String postId = extractNumericJsonValue(created.getResponse().getContentAsString(), "id");

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRejectsPostsOwnedByAnotherUser() throws Exception {
        String accessToken = loginAndExtract("accessToken");

        mockMvc.perform(delete("/api/posts/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    private String loginAndExtract(String fieldName) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier":"a",
                                  "password":"a"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return extractJsonValue(login.getResponse().getContentAsString(), fieldName);
    }

    private String extractJsonValue(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Field not found in JSON response: " + fieldName);
        }
        return matcher.group(1);
    }

    private String extractNumericJsonValue(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Numeric field not found in JSON response: " + fieldName);
        }
        return matcher.group(1);
    }
}
