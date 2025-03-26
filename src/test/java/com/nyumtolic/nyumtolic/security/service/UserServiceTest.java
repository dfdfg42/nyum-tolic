package com.nyumtolic.nyumtolic.security.service;

import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
import com.nyumtolic.nyumtolic.security.dto.UserCreateForm;
import com.nyumtolic.nyumtolic.security.dto.UserDTO;
import com.nyumtolic.nyumtolic.security.dto.UserUpdateForm;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private SessionRegistry sessionRegistry;

    @InjectMocks
    private UserService userService;

    private UserCreateForm userCreateForm;
    private SiteUser testUser;

    @BeforeEach
    void setUp() {
        // Set up test user creation form
        userCreateForm = new UserCreateForm();
        userCreateForm.setLoginId("testuser@example.com");
        userCreateForm.setNickname("Test User");
        userCreateForm.setEmail("testuser@example.com");
        userCreateForm.setPassword1("password123");
        userCreateForm.setPassword2("password123");

        // Set up existing test user
        testUser = SiteUser.builder()
                .id(1L)
                .loginId("testuser@example.com")
                .nickname("Test User")
                .email("testuser@example.com")
                .password("encodedPassword")
                .enabled(true)
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("Create user successfully")
    void create() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(SiteUser.class))).thenReturn(testUser);

        // Act
        SiteUser result = userService.create(userCreateForm);

        // Assert
        assertNotNull(result);
        assertEquals("testuser@example.com", result.getLoginId());
        assertEquals("Test User", result.getNickname());
        assertEquals("testuser@example.com", result.getEmail());
        assertTrue(result.getEnabled());
        assertEquals(UserRole.USER, result.getRole());
        assertEquals("encodedPassword", result.getPassword());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(SiteUser.class));
    }

    @Test
    @DisplayName("Get user by login ID")
    void getUser() {
        // Arrange
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(testUser));

        // Act
        SiteUser result = userService.getUser("testuser@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getLoginId(), result.getLoginId());
        verify(userRepository, times(1)).findByLoginId("testuser@example.com");
    }

    @Test
    @DisplayName("Get user by login ID - not found")
    void getUserNotFound() {
        // Arrange
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class, () -> {
            userService.getUser("nonexistent@example.com");
        });
        verify(userRepository, times(1)).findByLoginId("nonexistent@example.com");
    }

    @Test
    @DisplayName("Ban user")
    void banUser() {
        // Arrange
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SiteUser.class))).thenReturn(testUser);

        // Mock session registry
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        
        List<Object> loggedUsers = new ArrayList<>();
        loggedUsers.add(userDetails);
        
        SessionInformation sessionInfo = mock(SessionInformation.class);
        List<SessionInformation> sessions = Arrays.asList(sessionInfo);
        
        when(sessionRegistry.getAllPrincipals()).thenReturn(loggedUsers);
        when(sessionRegistry.getAllSessions(any(), anyBoolean())).thenReturn(sessions);

        // Act
        SiteUser result = userService.banUser("testuser@example.com");

        // Assert
        assertNotNull(result);
        assertFalse(result.getEnabled());
        verify(userRepository, times(1)).findByLoginId("testuser@example.com");
        verify(userRepository, times(1)).save(testUser);
        verify(sessionRegistry, times(1)).getAllPrincipals();
        verify(sessionRegistry, times(1)).getAllSessions(userDetails, false);
        verify(sessionInfo, times(1)).expireNow();
    }

    @Test
    @DisplayName("Ban nonexistent user")
    void banNonexistentUser() {
        // Arrange
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            userService.banUser("nonexistent@example.com");
        });
        verify(userRepository, times(1)).findByLoginId("nonexistent@example.com");
        verify(userRepository, never()).save(any(SiteUser.class));
    }

    @Test
    @DisplayName("Update user")
    void updateUser() {
        // Arrange
        UserUpdateForm updateForm = new UserUpdateForm();
        updateForm.setNickname("Updated Nickname");
        
        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SiteUser.class))).thenReturn(testUser);
        
        // Mock authentication
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = new TestingAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        SiteUser result = userService.updateUser("testuser@example.com", updateForm);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Nickname", result.getNickname());
        verify(userRepository, times(1)).findByLoginId("testuser@example.com");
        verify(userRepository, times(1)).save(testUser);
        
        // Clean up security context
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Get all user DTOs")
    void getAllUserDTOs() {
        // Arrange
        SiteUser user1 = SiteUser.builder()
                .id(1L)
                .loginId("user1@example.com")
                .nickname("User 1")
                .build();
        
        SiteUser user2 = SiteUser.builder()
                .id(2L)
                .loginId("user2@example.com")
                .nickname("User 2")
                .build();
        
        List<SiteUser> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDTO> result = userService.getAllUserDTOs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        UserDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("user1@example.com", dto1.getLoginId());
        assertEquals("User 1", dto1.getNickname());
        
        UserDTO dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("user2@example.com", dto2.getLoginId());
        assertEquals("User 2", dto2.getNickname());
        
        verify(userRepository, times(1)).findAll();
    }
}