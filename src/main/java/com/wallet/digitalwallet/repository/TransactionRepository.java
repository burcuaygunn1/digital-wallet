package com.wallet.digitalwallet.repository;

import com.wallet.digitalwallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t WHERE t.fromIban = :iban OR t.toIban = :iban ORDER BY t.createdAt DESC")
    List<Transaction> findAllByIbanOrderByCreatedAtDesc(@Param("iban") String iban);
    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "(t.fromIban IN :ibans OR t.toIban IN :ibans) " +
            "AND (t.createdAt < :date OR (t.createdAt = :date AND t.id <= :id))")
    long countUserTransactionsUntil(@Param("ibans") List<String> ibans,
                                    @Param("date") LocalDateTime date,
                                    @Param("id") Long id);
}