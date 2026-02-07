package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.CreatorUserModel;
import net.ikwa.techconnect.model.Product;
import net.ikwa.techconnect.repo.ProductRepository;
import net.ikwa.techconnect.repo.TechRegRepo;
import net.ikwa.techconnect.userregDTO.ProductRequest;
import net.ikwa.techconnect.userregDTO.ProductResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final TechRegRepo userRepository; // this handles CreatorUserModel

    public ProductResponse createProduct(Integer userId, ProductRequest request) {

        CreatorUserModel creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .type(request.getType())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDate.now())
                .creator(creator)
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    public List<ProductResponse> getCreatorProducts(Integer userId) {

        CreatorUserModel creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        return productRepository.findByCreator(creator)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .type(product.getType().name())
                .imageUrl(product.getImageUrl())
                .views(product.getViews())
                .sales(product.getSales())
                .rating(product.getRating())
                .reviews(product.getReviews())
                .createdAt(product.getCreatedAt().toString())
                .build();
    }
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setType(request.getType());
        product.setImageUrl(request.getImageUrl());

        productRepository.save(product);

        return mapToResponse(product);
    }


}
