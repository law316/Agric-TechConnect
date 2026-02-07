package net.ikwa.techconnect.repo;

import net.ikwa.techconnect.model.CreatorUserModel;
import net.ikwa.techconnect.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Get all products for a creator
    List<Product> findByCreator(CreatorUserModel creator);
}
