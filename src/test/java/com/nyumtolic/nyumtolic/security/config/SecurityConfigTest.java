package com.nyumtolic.nyumtolic.security.config;

import com.nyumtolic.nyumtolic.security.filter.IpBlackListFilter;
import com.nyumtolic.nyumtolic.security.service.IpService;
import com.nyumtolic.nyumtolic.security.service.PrincipalOauth2UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    private IpService ipService;

    @Test
    @DisplayName("Security configuration beans are created")
    void securityConfigurationBeansCreated() throws Exception {
        // Verify that beans are created
        assertNotNull(securityConfig.authenticationManager(null));
        assertNotNull(securityConfig.concurrentSession());
        assertNotNull(securityConfig.sessionInformationExpiredStrategy());
        assertNotNull(securityConfig.sessionRegistry());
    }

    @Test
    @DisplayName("CompositeSessionAuthenticationStrategy is properly configured")
    void compositeSessionAuthenticationStrategyConfiguration() {
        // Arrange
        CompositeSessionAuthenticationStrategy compositeStrategy = securityConfig.concurrentSession();
        
        // Assert
        assertNotNull(compositeStrategy);
        // The composite strategy should have delegated strategies
        assertTrue(compositeStrategy.toString().contains("delegateStrategies"));
    }

    @Test
    @DisplayName("SessionRegistry is properly configured")
    void sessionRegistryConfiguration() {
        // Arrange & Act
        SessionRegistry sessionRegistry = securityConfig.sessionRegistry();
        
        // Assert
        assertNotNull(sessionRegistry);
    }

    @Test
    @DisplayName("SessionInformationExpiredStrategy is properly configured")
    void sessionInformationExpiredStrategyConfiguration() {
        // Arrange & Act
        SessionInformationExpiredStrategy strategy = securityConfig.sessionInformationExpiredStrategy();
        
        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof CustomSessionInformationExpiredStrategy);
    }

    @Test
    @DisplayName("Public URLs are accessible without authentication")
    void publicUrlsAccessibleWithoutAuthentication() throws Exception {
        // Test access to public page
        mockMvc.perform(get("/"))
               .andExpect(status().isOk());

        // Test access to login page
        mockMvc.perform(get("/user/login"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login page redirects unauthenticated users")
    void loginPageRedirectsUnauthenticatedUsers() throws Exception {
        // Test access to a page that requires authentication
        mockMvc.perform(get("/mypage"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/user/login"));
    }
}