package com.nyumtolic.nyumtolic.security.repository;

import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByLoginId(String loginId);
}
