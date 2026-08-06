package com.wallet.digitalwallet.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private int totalWallets;
}