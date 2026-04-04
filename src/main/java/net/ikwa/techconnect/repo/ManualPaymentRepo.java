package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.ManualPaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualPaymentRepo extends JpaRepository<ManualPaymentModel, Long> {

    List<ManualPaymentModel> findByUserIdOrderByCreatedAtDesc(Integer userId);
}