package com.nyumtolic.nyumtolic.security.service;


import com.nyumtolic.nyumtolic.DataNotFoundException;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import com.nyumtolic.nyumtolic.security.domain.UserRole;
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

    public SiteUser create(String loginId, String email, String password){
        SiteUser user = SiteUser.builder()
                .loginId(loginId)
                .email(email)
                .password(passwordEncoder.encode(password))
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
