package com.wallet.digitalwallet.repository;

import com.wallet.digitalwallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByIban(String iban);

    List<Wallet> findByUserId(Long userId);

    boolean existsByIban(String iban);

    /**
     * Kullanıcının e-posta adresi üzerinden cüzdanlarını getiren özel sorgu.
     * Spring Data JPA "UserEmail" kısmını otomatik olarak "user.email" ile eşleştirir.
     */
    List<Wallet> findByUserEmail(String email);

    // Para transferi sırasında race condition'ı önlemek için cüzdanı veritabanı seviyesinde kilitler
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.iban = :iban")
    Optional<Wallet> findByIbanWithLock(@Param("iban") String iban);
}