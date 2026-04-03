package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTxRef(String txRef);

    Optional<Transaction> findByFlutterwaveTransactionId(String flutterwaveTransactionId);

    List<Transaction> findByUserEmail(String userEmail);

    List<Transaction> findByUserIdAndPaymentType(Integer userId, String paymentType);
}