package com.wallet.digitalwallet.controller;

import com.wallet.digitalwallet.dto.TransactionResponse;
import com.wallet.digitalwallet.dto.TransferRequest;
import com.wallet.digitalwallet.dto.WalletResponse;
import com.wallet.digitalwallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletResponse>> getUserWallets(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@Valid @RequestBody TransferRequest request) {
        String result = walletService.transferMoney(request);
        return ResponseEntity.ok(result);
        }
        // YENİ: Cüzdan İşlem Geçmişi Endpoint'i
    @GetMapping("/{iban}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory (@PathVariable String iban){
         return ResponseEntity.ok(walletService.getTransactionHistory(iban));
        }

}
