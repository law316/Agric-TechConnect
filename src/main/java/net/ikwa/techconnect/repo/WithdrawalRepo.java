package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRepo extends JpaRepository<Withdrawal, Long> {

    Optional<Withdrawal> findByReference(String reference);

    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(Integer userId);

    boolean existsByUserIdAndStatusIn(Integer userId, List<String> statuses);
}