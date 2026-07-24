package com.wallet.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private String currency;
    private String transactionType; // TRANSFER, DEPOSIT vb.
    private String status;          // SUCCESS, FAILED
    private LocalDateTime createdAt;
    private String direction;       // "INCOMING" (Gelen) veya "OUTGOING" (Giden)
}