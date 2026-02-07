package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.service.PromoterReg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PromoterRegRepo extends JpaRepository<PromoterUserModel, Integer> {
    Optional<PromoterUserModel> findByEmail(String email);

    Optional<PromoterUserModel> findByReferralCode(String referralCode);
}
