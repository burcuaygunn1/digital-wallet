package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

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