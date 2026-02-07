package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.Transaction;
import net.ikwa.techconnect.model.PromoterUserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(PromoterUserModel user);
    List<Transaction> findByUserEmail(String email);

}
