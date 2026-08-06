package com.wallet.digitalwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailUpdateRequest {
    @NotBlank(message = "Yeni e-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta giriniz")
    private String newEmail;

    @NotBlank(message = "Şifrenizi girmelisiniz")
    private String currentPassword;
}