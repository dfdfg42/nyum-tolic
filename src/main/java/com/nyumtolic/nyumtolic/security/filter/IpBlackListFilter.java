package com.nyumtolic.nyumtolic.security.filter;

import com.nyumtolic.nyumtolic.security.service.IpService;
import com.nyumtolic.nyumtolic.security.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class IpBlackListFilter extends OncePerRequestFilter {
    private final IpService ipService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = IpUtil.getClientIp(request);

        if(ipService.isIpBlackList(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Your IP is blacklisted. Access denied.");
            return;
        }

        filterChain.doFilter(request, response);
    }


}
