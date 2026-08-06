package com.wallet.digitalwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExchangeRequest {

    @NotBlank(message = "Kaynak IBAN boş olamaz")
    private String fromIban;

    @NotBlank(message = "Hedef IBAN boş olamaz")
    private String toIban;

    @NotNull(message = "Tutar boş olamaz")
    @Positive(message = "Tutar pozitif olmalıdır")
    private BigDecimal amount; // ya da projenizdeki yapıya göre Double
}