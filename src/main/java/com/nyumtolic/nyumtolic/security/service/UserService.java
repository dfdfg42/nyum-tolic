package com.nyumtolic.nyumtolic.security.service;


import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
import com.nyumtolic.nyumtolic.security.dto.UserCreateForm;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        List<Object> loggedUsers = sessionRegistry.getAllPrincipals(); // todo 현재 세션을 못 불러옴

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
}
