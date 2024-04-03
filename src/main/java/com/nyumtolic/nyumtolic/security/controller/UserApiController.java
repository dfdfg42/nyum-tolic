package com.nyumtolic.nyumtolic.security.controller;

import com.nyumtolic.nyumtolic.security.dto.UserUpdateForm;
import com.nyumtolic.nyumtolic.security.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserApiController {

    private final UserService userService;

    @PostAuthorize("isAuthenticated()")
    @PostMapping("/update")
    public String update(@Valid UserUpdateForm userUpdateForm, Principal principal, BindingResult bindingResult) {

        try {
            userService.updateUser(principal.getName(), userUpdateForm);
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("updateFailed", e.getMessage());
        }

        return userUpdateForm.getNickname();
    }
}
