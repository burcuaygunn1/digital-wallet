package com.wallet.digitalwallet.repository;

import com.wallet.digitalwallet.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSpecification {

    public static Specification<Transaction> hasIban(String iban) {
        return (root, query, cb) -> {
            if (iban == null || iban.isBlank()) return cb.conjunction();
            return cb.or(
                    cb.equal(root.get("fromIban"), iban),
                    cb.equal(root.get("toIban"), iban)
            );
        };
    }

    public static Specification<Transaction> amountGreaterThanOrEqual(BigDecimal minAmount) {
        return (root, query, cb) ->
                minAmount == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> amountLessThanOrEqual(BigDecimal maxAmount) {
        return (root, query, cb) ->
                maxAmount == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<Transaction> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            if (startDate != null && endDate != null) return cb.between(root.get("createdAt"), startDate, endDate);
            if (startDate != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }
}