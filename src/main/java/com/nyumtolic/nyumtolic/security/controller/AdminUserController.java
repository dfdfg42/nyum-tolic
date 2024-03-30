package com.nyumtolic.nyumtolic.security.controller;

import com.nyumtolic.nyumtolic.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user/admin")
public class AdminUserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{loginId}/ban")
    public String ban(@PathVariable String loginId) {
        userService.banUser(loginId);
        return "redirect:/";
    }
}
