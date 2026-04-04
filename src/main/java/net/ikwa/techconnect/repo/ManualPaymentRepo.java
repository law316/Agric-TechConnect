package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.ManualPaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualPaymentRepo extends JpaRepository<ManualPaymentModel, Long> {

    List<ManualPaymentModel> findByUserIdOrderByCreatedAtDesc(Integer userId);


    Optional<ManualPaymentModel> findFirstByUserIdOrderByCreatedAtDesc(Integer userId);

    boolean existsByUserIdAndStatus(Integer userId, String status);
}