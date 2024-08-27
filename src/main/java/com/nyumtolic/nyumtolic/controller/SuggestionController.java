package com.nyumtolic.nyumtolic.controller;



import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SuggestionController {


    private final JavaMailSender mailSender;

    @PostMapping("/suggestions")
    public ResponseEntity<?> sendSuggestion(@RequestParam String name,
                                            @RequestParam String email,
                                            @RequestParam String suggestionText) {
        try {
            sendSuggestionEmail(name, email, suggestionText);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send suggestion email");
        }
    }

    private void sendSuggestionEmail(String name, String email, String suggestionText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("7292you@naver.com"); // Set sender email
        message.setTo("7292you@naver.com"); // Set recipient email
        message.setSubject("New Suggestion from " + name);
        message.setText("Name: " + name + "\nEmail: " + email + "\nSuggestion: " + suggestionText);
        mailSender.send(message);
    }
}
