package com.nyumtolic.nyumtolic.security.repository;

import com.nyumtolic.nyumtolic.security.domain.IpBlackList;
import com.nyumtolic.nyumtolic.security.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IpRepository extends JpaRepository<IpBlackList, Long> {
    boolean existsByIp(String ip);
}
