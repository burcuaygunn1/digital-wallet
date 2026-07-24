package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.TransactionResponse;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService; // Döviz servisi inject edildi

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

    // Para Transfer İşlemi (Döviz Dönüşümlü & ACID - Transactional Güvencesiyle)
    @Transactional
    public String transferMoney(TransferRequest request) {
        if (request.getFromIban().equals(request.getToIban())) {
            throw new BusinessException("Aynı cüzdana transfer yapılamaz.");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer tutarı 0'dan büyük olmalıdır.");
        }

        // Kilitli veritabanı sorguları (Race condition koruması)
        Wallet fromWallet = walletRepository.findByIbanWithLock(request.getFromIban())
                .orElseThrow(() -> new ResourceNotFoundException("Gönderen cüzdan bulunamadı: " + request.getFromIban()));

        Wallet toWallet = walletRepository.findByIbanWithLock(request.getToIban())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı cüzdan bulunamadı: " + request.getToIban()));

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Yetersiz bakiye! Mevcut bakiye: " + fromWallet.getBalance());
        }

        // --- DÖVİZ ENTEGRASYONU AŞAMASI ---
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromWallet.getCurrency(), toWallet.getCurrency());
        BigDecimal convertedAmount = exchangeRateService.convert(request.getAmount(), exchangeRate);

        // Gönderenin bakiyesinden gönderdiği tutar düşer
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));

        // Alıcının bakiyesine kur ile dönüştürülmüş tutar eklenir
        toWallet.setBalance(toWallet.getBalance().add(convertedAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // İşlem kaydı oluştur
        Transaction transaction = Transaction.builder()
                .fromIban(fromWallet.getIban())
                .toIban(toWallet.getIban())
                .amount(request.getAmount())
                .currency(fromWallet.getCurrency())
                .transactionType("TRANSFER")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return String.format("Transfer başarıyla tamamlandı. Dönüştürülen Tutar: %s %s (Kur: %s)",
                convertedAmount, toWallet.getCurrency(), exchangeRate);
    }

    // Kullanıcının Cüzdanına Ait İşlem Geçmişini Getir
    public List<TransactionResponse> getTransactionHistory(String iban) {
        // Cüzdanın varlığını doğrula
        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Cüzdan bulunamadı: " + iban));

        List<Transaction> transactions = transactionRepository.findAllByIbanOrderByCreatedAtDesc(iban);

        return transactions.stream().map(t -> {
            // İşlem bu IBAN için GELEN mi yoksa GİDEN mi kontrolü
            String direction = t.getFromIban().equals(iban) ? "OUTGOING" : "INCOMING";

            return TransactionResponse.builder()
                    .id(t.getId())
                    .fromIban(t.getFromIban())
                    .toIban(t.getToIban())
                    .amount(t.getAmount())
                    .currency(t.getCurrency())
                    .transactionType(t.getTransactionType())
                    .status(t.getStatus())
                    .createdAt(t.getCreatedAt())
                    .direction(direction)
                    .build();
        }).toList();
    }
}