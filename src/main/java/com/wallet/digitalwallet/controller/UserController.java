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

    /**
     * Yeni Kullanıcı Kaydı Endpoint'i (Public)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Kullanıcı Giriş Endpoint'i (Public)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Şifre Güncelleme Endpoint'i (Protected)
     */
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

    /**
     * Kullanıcı Profil Bilgilerini Getirme Endpoint'i (Protected)
     * GET /api/v1/users/me/profile
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }

    /**
     * Kullanıcı Profil Bilgilerini Güncelleme Endpoint'i (Ad / Soyad) (Protected)
     * PUT /api/v1/users/me/profile
     */
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

    /**
     * Kullanıcı E-Posta Güncelleme Endpoint'i (Protected)
     * PUT /api/v1/users/me/email
     * E-posta değiştiği için taze JWT token döner.
     */
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