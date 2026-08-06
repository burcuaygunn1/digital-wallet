package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.*;
import com.wallet.digitalwallet.entity.User;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.BusinessException;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.UserRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import com.wallet.digitalwallet.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda: " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        // Otomatik Cüzdan Oluşturma
        Wallet defaultWallet = Wallet.builder()
                .iban(generateRandomIban())
                .currency("TRY")
                .balance(new BigDecimal("1000.00"))
                .user(savedUser)
                .build();

        walletRepository.save(defaultWallet);

        String token = jwtService.generateToken(savedUser.getEmail());
        return new AuthResponse(token, savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("E-posta veya şifre hatalı."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("E-posta veya şifre hatalı.");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Mevcut şifreniz hatalı!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        return UserProfileResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .totalWallets(user.getWallets() != null ? user.getWallets().size() : 0)
                .build();
    }

    @Transactional
    public void updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);
    }

    /**
     * Kullanıcı E-Posta Güncelleme (Şifre Doğrulamalı)
     */
    @Transactional
    public AuthResponse updateEmail(String currentEmail, EmailUpdateRequest request) {
        // 1. Kullanıcıyı bul
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // 2. Şifreyi doğrula (Güvenlik Katmanı)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mevcut şifreniz hatalı! Email değiştirilemedi.");
        }

        // 3. Yeni email kullanımda mı kontrol et
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten başka bir hesap tarafından kullanılıyor.");
        }

        // 4. Güncelleme ve kaydet
        user.setEmail(request.getNewEmail());
        userRepository.save(user);

        // 5. Yeni email ile taze Token üretimi
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }

    private String generateRandomIban() {
        return "TR" + String.format("%022d",
                Math.abs(UUID.randomUUID().getLeastSignificantBits() % 1000000000000000000L));
    }
}