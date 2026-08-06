package com.wallet.digitalwallet.controller;

import com.wallet.digitalwallet.dto.DepositRequest;
import com.wallet.digitalwallet.dto.ExchangeRequest; // Döviz dönüştürme DTO'su varsa
import com.wallet.digitalwallet.dto.TransactionResponse;
import com.wallet.digitalwallet.dto.TransferRequest;
import com.wallet.digitalwallet.dto.WalletResponse;
import com.wallet.digitalwallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Giriş yapmış kullanıcının TÜM cüzdanlarını liste olarak döner.
     * GET /api/v1/wallets/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<WalletResponse>> getMyWallet(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        List<WalletResponse> wallets = walletService.getUserWalletsByEmail(email);

        if (wallets == null || wallets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Çoklu cüzdan desteği içinListenin tamamını dönüyoruz
        return ResponseEntity.ok(wallets);
    }

    /**
     * Belirli bir kullanıcı ID'sine ait cüzdanları getirir.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletResponse>> getUserWallets(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    /**
     * Hesaba Bakiye Yükleme Endpoint'i
     * POST /api/v1/wallets/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@Valid @RequestBody DepositRequest request) {
        walletService.depositMoney(request.getToIban(), request.getAmount());
        return ResponseEntity.ok("Bakiye başarıyla yüklendi.");
    }

    /**
     * Cüzdanlar Arası Transfer Endpoint'i
     * POST /api/v1/wallets/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@Valid @RequestBody TransferRequest request) {
        String result = walletService.transferMoney(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Yeni Cüzdan Oluşturma Endpoint'i (TRY, USD, EUR vb.)
     * POST /api/v1/wallets/create?currency=USD
     */
    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(Principal principal, @RequestParam String currency) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WalletResponse newWallet = walletService.createWalletForUser(principal.getName(), currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
    }

    /**
     * Döviz Dönüştürme / Cüzdanlar Arası Çapraz Transfer Endpoint'i
     * POST /api/v1/wallets/exchange
     */
    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeMoney(Principal principal, @Valid @RequestBody ExchangeRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        walletService.exchangeMoney(principal.getName(), request);
        return ResponseEntity.ok("Döviz dönüşümü başarıyla yapıldı.");
    }

    /**
     * İşlem Geçmişi Endpoint'i
     * GET /api/v1/wallets/{iban}/transactions
     */
    @GetMapping("/{iban}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable String iban) {
        return ResponseEntity.ok(walletService.getTransactionHistory(iban));
    }
}