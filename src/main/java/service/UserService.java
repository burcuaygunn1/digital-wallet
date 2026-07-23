package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.UserRegisterRequest;
import com.wallet.digitalwallet.dto.UserResponse;
import com.wallet.digitalwallet.entity.User;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.BusinessException;
import com.wallet.digitalwallet.repository.UserRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // 1. E-posta adresi sistemde zaten var mı kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda: " + request.getEmail());
        }

        // 2. Yeni User Entity oluşturma
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword()) // İlerleyen adımlarda Spring Security ile şifreleyeceğiz
                .build();

        User savedUser = userRepository.save(user);

        // 3. Yeni kaydolan kullanıcıya otomatik TRY Cüzdanı (IBAN) tanımlama
        Wallet defaultWallet = Wallet.builder()
                .iban(generateRandomIban())
                .currency("TRY")
                .balance(BigDecimal.valueOf(1000.00)) // Hoş geldin bakiyesi (Test için 1000 TL)
                .user(savedUser)
                .build();

        walletRepository.save(defaultWallet);

        // 4. Güvenli DTO dönüşü
        return UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    // Rastgele TR ile başlayan 24 haneli benzersiz IBAN üreten yardımcı metod
    private String generateRandomIban() {
        Random random = new Random();
        StringBuilder iban = new StringBuilder("TR");
        for (int i = 0; i < 22; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }
}