package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.TransferRequest;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private WalletService walletService;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        senderWallet = new Wallet();
        senderWallet.setIban("TR111");
        senderWallet.setCurrency("TRY");
        senderWallet.setBalance(new BigDecimal("500.00"));

        receiverWallet = new Wallet();
        receiverWallet.setIban("TR222");
        receiverWallet.setCurrency("TRY");
        receiverWallet.setBalance(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Başarılı Para Transferi Senaryosu")
    void transfer_SuccessfulTransfer() {
        TransferRequest request = new TransferRequest();
        request.setFromIban("TR111");
        request.setToIban("TR222");
        request.setAmount(new BigDecimal("100.00"));

        when(walletRepository.findByIbanWithLock("TR111")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIbanWithLock("TR222")).thenReturn(Optional.of(receiverWallet));
        when(exchangeRateService.getExchangeRate("TRY", "TRY")).thenReturn(BigDecimal.ONE);
        when(exchangeRateService.convert(new BigDecimal("100.00"), BigDecimal.ONE)).thenReturn(new BigDecimal("100.00"));
        assertDoesNotThrow(() -> walletService.transferMoney(request));
        assertEquals(new BigDecimal("400.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("200.00"), receiverWallet.getBalance());

        verify(walletRepository, times(1)).save(senderWallet);
        verify(walletRepository, times(1)).save(receiverWallet);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Yetersiz Bakiye Durumunda Hata Fırlatmalı")
    void transfer_InsufficientBalance_ShouldThrowException() {
        TransferRequest request = new TransferRequest();
        request.setFromIban("TR111");
        request.setToIban("TR222");
        request.setAmount(new BigDecimal("1000.00")); // Bakiyeden fazla tutar (Bakiye: 500)

        when(walletRepository.findByIbanWithLock("TR111")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIbanWithLock("TR222")).thenReturn(Optional.of(receiverWallet));
        assertThrows(RuntimeException.class, () -> walletService.transferMoney(request));
        verify(walletRepository, never()).save(any());
    }
}