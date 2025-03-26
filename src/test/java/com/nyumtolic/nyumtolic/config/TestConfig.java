package com.nyumtolic.nyumtolic.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cloudinary.Cloudinary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration class that provides mock beans for testing purposes.
 * This configuration is only active in the "test" profile.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
    
    /**
     * Creates a mock AmazonS3Client bean for testing.
     * This prevents real AWS calls from being made during tests.
     *
     * @return A mock AmazonS3Client
     */
    @Bean
    @Primary
    public AmazonS3Client amazonS3Client() {
        // Create a mock S3 client that won't make real AWS calls
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:9000", "us-east-1"))
                .build();
    }
    
    /**
     * Creates a mock Cloudinary bean for testing.
     * This prevents real Cloudinary API calls from being made during tests.
     *
     * @return A mock Cloudinary instance
     */
    @Bean
    @Primary
    public Cloudinary cloudinary() {
        // Create a mock Cloudinary instance with dummy config
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "test-cloud");
        config.put("api_key", "test-key");
        config.put("api_secret", "test-secret");
        return new Cloudinary(config);
    }
    
    /**
     * Creates a BCryptPasswordEncoder with a low strength for faster test execution.
     *
     * @return A BCryptPasswordEncoder with strength 4
     */
    @Bean
    @Primary
    public BCryptPasswordEncoder testPasswordEncoder() {
        // Use a low strength for faster test execution
        return new BCryptPasswordEncoder(4);
    }
}