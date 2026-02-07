package net.ikwa.techconnect.controllers;



import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.userregDTO.ProductRequest;
import net.ikwa.techconnect.userregDTO.ProductResponse;
import net.ikwa.techconnect.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    // ✅ CREATE
    @PostMapping("/creator/{userId}")
    public ProductResponse createProduct(
            @PathVariable Integer userId,
            @RequestBody ProductRequest request) {
        return productService.createProduct(userId, request);
    }

    // ✅ GET ALL PRODUCTS FOR CREATOR (Frontend Dashboard)
    @GetMapping("/creator/{userId}")
    public List<ProductResponse> getCreatorProducts(@PathVariable Integer userId) {
        return productService.getCreatorProducts(userId);
    }

    // ✅ UPDATE
    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable Integer productId,
            @RequestBody ProductRequest request) {
        return productService.updateProduct(productId, request);
    }
}
