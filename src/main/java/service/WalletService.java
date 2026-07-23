package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.TransferRequest;
import com.wallet.digitalwallet.dto.WalletResponse;
import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.BusinessException;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // Kullanıcının Cüzdanlarını Getir
    public List<WalletResponse> getUserWallets(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(wallet -> WalletResponse.builder()
                        .id(wallet.getId())
                        .iban(wallet.getIban())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .build())
                .toList();
    }

    // Para Transfer İşlemi (ACID - Transactional Güvencesiyle)
    @Transactional
    public String transferMoney(TransferRequest request) {
        // 1. Aynı IBAN'a transfer engeli
        if (request.getFromIban().equals(request.getToIban())) {
            throw new RuntimeException("Kendi cüzdanınıza transfer yapamazsınız.");
        }

        // 2. Cüzdanların varlığını kontrol et
        Wallet fromWallet = walletRepository.findByIban(request.getFromIban())
                .orElseThrow(() -> new ResourceNotFoundException("Gönderen cüzdan bulunamadı: " + request.getFromIban()));

        Wallet toWallet = walletRepository.findByIban(request.getToIban())
                .orElseThrow(() -> new RuntimeException("Alıcı cüzdan bulunamadı: " + request.getToIban()));

        // 3. Para birimi uyumluluğu kontrolü
        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new RuntimeException("Farklı para birimleri arasında doğrudan transfer yapılamaz.");
        }

        // 4. Bakiye yeterli mi kontrolü
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Yetersiz bakiye! Mevcut bakiye: " + fromWallet.getBalance());
        }

        // 5. Bakiyeleri güncelle (Gönderenden düş, alıcıya ekle)
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // 6. İşlem geçmişini (Transaction) kaydet
        Transaction transaction = Transaction.builder()
                .fromIban(fromWallet.getIban())
                .toIban(toWallet.getIban())
                .amount(request.getAmount())
                .currency(fromWallet.getCurrency())
                .transactionType("TRANSFER")
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);

        return "Transfer başarıyla gerçekleşti. Gönderilen Tutar: " + request.getAmount() + " " + fromWallet.getCurrency();
    }
}