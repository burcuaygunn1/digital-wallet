package com.wallet.digitalwallet.repository;

import com.wallet.digitalwallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    // Belirli bir IBAN'ın hem gönderici hem alıcı olduğu işlemleri en yeni tarihten eskiye sıralayarak getirir
    @Query("SELECT t FROM Transaction t WHERE t.fromIban = :iban OR t.toIban = :iban ORDER BY t.createdAt DESC")
    List<Transaction> findAllByIbanOrderByCreatedAtDesc(@Param("iban") String iban);
}