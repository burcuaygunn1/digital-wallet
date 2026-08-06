package com.wallet.digitalwallet.repository;

import com.wallet.digitalwallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Kullanıcıyı ve ilişkilendirilmiş cüzdanlarını (wallets) tek bir SQL sorgusunda getirir.
     * TransactionService üzerindeki hatayı çözer ve N+1 problemini önler.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.wallets WHERE u.email = :email")
    Optional<User> findByEmailWithWallets(@Param("email") String email);
}