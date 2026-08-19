package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.entity.User;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.TransactionSpecification;
import com.wallet.digitalwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    
    public Page<Transaction> getMyTransactions(String email, Pageable pageable) {
        return getMyFilteredTransactions(email, null, null, null, null, pageable);
    }

    
    public Page<Transaction> getMyFilteredTransactions(
            String email,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        User user = userRepository.findByEmailWithWallets(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));

        List<String> ibans = user.getWallets().stream()
                .map(Wallet::getIban)
                .toList();

        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.hasAnyIbans(ibans))
                .and(TransactionSpecification.amountGreaterThanOrEqual(minAmount))
                .and(TransactionSpecification.amountLessThanOrEqual(maxAmount))
                .and(TransactionSpecification.createdBetween(startDate, endDate));

        return transactionRepository.findAll(spec, pageable);
    }

    
    public Page<Transaction> getFilteredTransactions(
            String iban,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.hasIban(iban))
                .and(TransactionSpecification.amountGreaterThanOrEqual(minAmount))
                .and(TransactionSpecification.amountLessThanOrEqual(maxAmount))
                .and(TransactionSpecification.createdBetween(startDate, endDate));

        return transactionRepository.findAll(spec, pageable);
    }
}