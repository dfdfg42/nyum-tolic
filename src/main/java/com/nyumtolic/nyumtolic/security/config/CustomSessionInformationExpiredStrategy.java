package com.nyumtolic.nyumtolic.security.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

public class CustomSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {
    private final String expiredUrl = "";

    public CustomSessionInformationExpiredStrategy() {}

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent sessionInformationExpiredEvent) throws IOException, ServletException {
        HttpServletRequest request = sessionInformationExpiredEvent.getRequest();
        HttpServletResponse response = sessionInformationExpiredEvent.getResponse();
        request.getSession();// creates a new session
        response.sendRedirect(expiredUrl);
    }
}
