package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.CreatorUserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechRegRepo extends JpaRepository<CreatorUserModel, Integer> {
    Optional<CreatorUserModel> findByEmail(String email);
}
