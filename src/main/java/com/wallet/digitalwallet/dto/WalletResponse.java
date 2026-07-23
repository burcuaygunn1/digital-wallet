package com.wallet.digitalwallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletResponse {
    private Long id;
    private String iban;
    private String currency;
    private BigDecimal balance;
}