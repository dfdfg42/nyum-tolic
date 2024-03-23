package com.nyumtolic.nyumtolic.security.service;


import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
import com.nyumtolic.nyumtolic.security.dto.UserCreateForm;
import com.nyumtolic.nyumtolic.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
}
