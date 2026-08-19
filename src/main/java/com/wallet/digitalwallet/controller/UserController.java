package com.wallet.digitalwallet.controller;

import com.wallet.digitalwallet.dto.*;
import com.wallet.digitalwallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            Principal principal,
            @Valid @RequestBody PasswordUpdateRequest request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updatePassword(principal.getName(), request);
        return ResponseEntity.ok("Şifre başarıyla güncellendi.");
    }

    
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }

    
    @PutMapping("/me/profile")
    public ResponseEntity<String> updateProfile(
            Principal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok("Profil başarıyla güncellendi.");
    }

    
    @PutMapping("/me/email")
    public ResponseEntity<AuthResponse> updateEmail(
            Principal principal,
            @Valid @RequestBody EmailUpdateRequest request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResponse response = userService.updateEmail(principal.getName(), request);
        return ResponseEntity.ok(response);
    }
}