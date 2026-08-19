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
        return ResponseEntity.ok(wallets);
    }

    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletResponse>> getUserWallets(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@Valid @RequestBody DepositRequest request) {
        walletService.depositMoney(request.getToIban(), request.getAmount());
        return ResponseEntity.ok("Bakiye başarıyla yüklendi.");
    }

    
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@Valid @RequestBody TransferRequest request) {
        String result = walletService.transferMoney(request);
        return ResponseEntity.ok(result);
    }

    
    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(Principal principal, @RequestParam String currency) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WalletResponse newWallet = walletService.createWalletForUser(principal.getName(), currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
    }

    
    @PostMapping("/exchange")
    public ResponseEntity<String> exchangeMoney(Principal principal, @Valid @RequestBody ExchangeRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        walletService.exchangeMoney(principal.getName(), request);
        return ResponseEntity.ok("Döviz dönüşümü başarıyla yapıldı.");
    }

    
    @GetMapping("/{iban}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable String iban) {
        return ResponseEntity.ok(walletService.getTransactionHistory(iban));
    }
}