package com.nyumtolic.nyumtolic.security.service;

import com.nyumtolic.nyumtolic.security.repository.IpRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IpService {
    private final IpRepository ipRepository;

    public boolean isIpBlackList(String ip) {
        return ipRepository.existsByIp(ip);
    }
}
