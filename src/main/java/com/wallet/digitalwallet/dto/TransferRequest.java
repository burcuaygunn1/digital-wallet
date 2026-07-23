package com.wallet.digitalwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Gönderen IBAN boş olamaz")
    private String fromIban;

    @NotBlank(message = "Alıcı IBAN boş olamaz")
    private String toIban;

    @NotNull(message = "Tutar boş olamaz")
    @DecimalMin(value = "0.01", message = "Transfer tutarı en az 0.01 olmalıdır")
    private BigDecimal amount;
}