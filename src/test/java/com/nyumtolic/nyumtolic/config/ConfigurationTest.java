package com.nyumtolic.nyumtolic.config;

import com.nyumtolic.nyumtolic.api.config.SwaggerConfig;
import com.nyumtolic.nyumtolic.cloudinary.CloudinaryConfig;
import com.nyumtolic.nyumtolic.s3.S3Config;
import com.nyumtolic.nyumtolic.security.config.BCryptConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SwaggerConfig swaggerConfig;

    @Autowired
    private BCryptConfig bCryptConfig;

    // S3Config and CloudinaryConfig may not be testable without proper credentials,
    // so we just verify they can be autowired without errors

    @Test
    @DisplayName("All configuration beans are created")
    void allConfigurationBeansAreCreated() {
        // Verify all config classes are created
        assertNotNull(context.getBean(SwaggerConfig.class));
        assertNotNull(context.getBean(BCryptConfig.class));
        
        // These may fail if credentials are not set
        try {
            assertNotNull(context.getBean(S3Config.class));
        } catch (Exception e) {
            // This is expected if S3 credentials are not set
            assertTrue(e.getMessage().contains("S3Config") || e.getMessage().contains("AmazonS3"));
        }
        
        try {
            assertNotNull(context.getBean(CloudinaryConfig.class));
        } catch (Exception e) {
            // This is expected if Cloudinary credentials are not set
            assertTrue(e.getMessage().contains("Cloudinary") || e.getMessage().contains("cloudinary"));
        }
    }

    @Test
    @DisplayName("Swagger configuration creates OpenAPI bean")
    void swaggerConfigurationCreatesOpenAPIBean() {
        // Create OpenAPI bean
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        
        // Verify OpenAPI bean is created with correct info
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("냠톨릭 API", openAPI.getInfo().getTitle());
        assertTrue(openAPI.getInfo().getDescription().contains("냠톨릭"));
    }

    @Test
    @DisplayName("BCrypt configuration creates password encoder")
    void bCryptConfigurationCreatesPasswordEncoder() {
        // Create BCryptPasswordEncoder bean
        BCryptPasswordEncoder passwordEncoder = bCryptConfig.passwordEncoder();
        
        // Verify BCryptPasswordEncoder bean is created
        assertNotNull(passwordEncoder);
        
        // Test encoding a password
        String password = "testPassword";
        String encodedPassword = passwordEncoder.encode(password);
        
        // Verify encoding works
        assertNotNull(encodedPassword);
        assertNotEquals(password, encodedPassword);
        assertTrue(passwordEncoder.matches(password, encodedPassword));
    }
}