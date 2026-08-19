package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.ExchangeRequest;
import com.wallet.digitalwallet.dto.TransactionResponse;
import com.wallet.digitalwallet.dto.TransferRequest;
import com.wallet.digitalwallet.dto.WalletResponse;
import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.entity.User;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.BusinessException;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.UserRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ExchangeRateService exchangeRateService;

    
    @Transactional(readOnly = true)
    public List<WalletResponse> getUserWalletsByEmail(String email) {
        return walletRepository.findByUserEmail(email).stream()
                .map(wallet -> WalletResponse.builder()
                        .id(wallet.getId())
                        .iban(wallet.getIban())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .build())
                .toList();
    }

    
    @Transactional(readOnly = true)
    public WalletResponse getWalletByEmail(String email) {
        return walletRepository.findByUserEmail(email).stream()
                .findFirst()
                .map(wallet -> WalletResponse.builder()
                        .id(wallet.getId())
                        .iban(wallet.getIban())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Cüzdan bulunamadı: " + email));
    }

    @Transactional(readOnly = true)
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

    
    @Transactional
    public WalletResponse createWalletForUser(String email, String currency) {
        return createNewWallet(email, currency);
    }

    
    @Transactional
    public WalletResponse createNewWallet(String email, String currency) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
        boolean exists = user.getWallets().stream()
                .anyMatch(w -> w.getCurrency().equalsIgnoreCase(currency));
        if (exists) {
            throw new BusinessException("Zaten " + currency.toUpperCase() + " biriminde bir cüzdanınız mevcut.");
        }

        Wallet newWallet = Wallet.builder()
                .iban(generateRandomIban())
                .currency(currency.toUpperCase())
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        Wallet saved = walletRepository.save(newWallet);
        return WalletResponse.builder()
                .id(saved.getId())
                .iban(saved.getIban())
                .currency(saved.getCurrency())
                .balance(saved.getBalance())
                .build();
    }

    
    @Transactional
    public String exchangeMoney(String email, ExchangeRequest request) {
        return exchangeCurrency(email, request.getFromIban(), request.getToIban(), request.getAmount());
    }

    
    @Transactional
    public String exchangeCurrency(String email, String fromIban, String toIban, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Dönüştürülecek tutar 0'dan büyük olmalıdır.");
        }

        if (fromIban.equals(toIban)) {
            throw new BusinessException("Aynı cüzdan arasında döviz değişimi yapılamaz.");
        }

        Wallet fromWallet = walletRepository.findByIbanWithLock(fromIban)
                .orElseThrow(() -> new ResourceNotFoundException("Kaynak cüzdan bulunamadı: " + fromIban));
        Wallet toWallet = walletRepository.findByIbanWithLock(toIban)
                .orElseThrow(() -> new ResourceNotFoundException("Hedef cüzdan bulunamadı: " + toIban));
        if (!fromWallet.getUser().getEmail().equals(email) || !toWallet.getUser().getEmail().equals(email)) {
            throw new BusinessException("Sadece kendi cüzdanlarınız arasında dönüşüm yapabilirsiniz.");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Yetersiz bakiye! Mevcut bakiye: " + fromWallet.getBalance());
        }
        BigDecimal rate = exchangeRateService.getExchangeRate(fromWallet.getCurrency(), toWallet.getCurrency());
        BigDecimal convertedAmount = exchangeRateService.convert(amount, rate);
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(convertedAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        transactionRepository.save(Transaction.builder()
                .fromIban(fromIban)
                .toIban(toIban)
                .amount(amount)
                .currency(fromWallet.getCurrency())
                .transactionType("EXCHANGE")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build());

        return String.format("%.2f %s başarıyla %.2f %s birimine dönüştürüldü (Kur: %s)",
                amount, fromWallet.getCurrency(), convertedAmount, toWallet.getCurrency(), rate);
    }

    
    @Transactional
    public void depositMoney(String iban, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Yüklenecek tutar 0'dan büyük olmalıdır.");
        }

        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Cüzdan bulunamadı: " + iban));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        Transaction transaction = Transaction.builder()
                .fromIban("SYSTEM_DEPOSIT")
                .toIban(iban)
                .amount(amount)
                .currency(wallet.getCurrency())
                .transactionType("DEPOSIT")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    
    @Transactional
    public String transferMoney(TransferRequest request) {
        if (request.getFromIban().equals(request.getToIban())) {
            throw new BusinessException("Aynı cüzdana transfer yapılamaz.");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer tutarı 0'dan büyük olmalıdır.");
        }

        Wallet fromWallet = walletRepository.findByIbanWithLock(request.getFromIban())
                .orElseThrow(() -> new ResourceNotFoundException("Gönderen cüzdan bulunamadı: " + request.getFromIban()));

        Wallet toWallet = walletRepository.findByIbanWithLock(request.getToIban())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı cüzdan bulunamadı: " + request.getToIban()));

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Yetersiz bakiye! Mevcut bakiye: " + fromWallet.getBalance());
        }

        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromWallet.getCurrency(), toWallet.getCurrency());
        BigDecimal convertedAmount = exchangeRateService.convert(request.getAmount(), exchangeRate);

        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(convertedAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

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

    
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String iban) {
        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Cüzdan bulunamadı: " + iban));

        List<Transaction> transactions = transactionRepository.findAllByIbanOrderByCreatedAtDesc(iban);

        return transactions.stream().map(t -> {
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

    
    private String generateRandomIban() {
        Random random = new Random();
        StringBuilder iban = new StringBuilder("TR");
        for (int i = 0; i < 24; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }
}