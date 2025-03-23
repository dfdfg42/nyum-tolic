package com.nyumtolic.nyumtolic.cloudinary;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(Environment env) {
        String cloudName = env.getProperty("cloudinary.cloud_name");
        String apiKey = env.getProperty("cloudinary.api_key");
        String apiSecret = env.getProperty("cloudinary.api_secret");


        Map<String, Object> configMap = new HashMap<>();
        configMap.put("cloud_name", cloudName);
        configMap.put("api_key", apiKey);
        configMap.put("api_secret", apiSecret);

        return new Cloudinary(configMap);
    }
}
