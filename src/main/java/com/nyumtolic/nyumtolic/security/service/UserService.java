package com.nyumtolic.nyumtolic.security.service;


import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
import com.nyumtolic.nyumtolic.security.dto.UserCreateForm;
import com.nyumtolic.nyumtolic.security.dto.UserDTO;
import com.nyumtolic.nyumtolic.security.dto.UserUpdateForm;
import com.nyumtolic.nyumtolic.security.oauth.PrincipalDetails;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    public SiteUser create(UserCreateForm userCreateForm){
        SiteUser user = SiteUser.builder()
                .loginId(userCreateForm.getLoginId())
                .nickname(userCreateForm.getNickname())
                .email(userCreateForm.getEmail())
                .password(passwordEncoder.encode(userCreateForm.getPassword1()))
                .enabled(true)
                .role(UserRole.USER)
                .build();
        this.userRepository.save(user);
        return user;
    }

    public SiteUser getUser(String loginId) {
        Optional<SiteUser> siteUser = this.userRepository.findByLoginId(loginId);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    public SiteUser banUser(String loginId) {
        SiteUser siteUser = userRepository.findByLoginId(loginId).orElseThrow(() ->  new ResponseStatusException(HttpStatus.BAD_REQUEST, "유저를 찾을 수 없습니다."));
        siteUser.setEnabled(false);
        userRepository.save(siteUser);

        List<Object> loggedUsers = sessionRegistry.getAllPrincipals();

        for (Object principal : loggedUsers) {
            UserDetails userDetails = (UserDetails) principal;
            if (userDetails.getUsername().equals(loginId)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
        return siteUser;
    }

    public SiteUser updateUser(String loginId, UserUpdateForm userUpdateForm) {
        SiteUser siteUser = userRepository.findByLoginId(loginId).orElseThrow(() ->  new ResponseStatusException(HttpStatus.BAD_REQUEST, "유저를 찾을 수 없습니다."));
        siteUser.setNickname(userUpdateForm.getNickname());
        userRepository.save(siteUser);

        // 세션 동기화
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        UserDetails updatedUserDetails = new PrincipalDetails(siteUser);
        Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(updatedUserDetails,
                authentication.getCredentials(),
                authentication.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);

        return siteUser;
    }

    public List<UserDTO> getAllUserDTOs() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(SiteUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setLoginId(user.getLoginId());
        dto.setNickname(user.getNickname());
        return dto;
    }
}
