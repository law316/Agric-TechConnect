package net.ikwa.techconnect.userregDTO;


import lombok.Data;
import net.ikwa.techconnect.enums.ProductType;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private ProductType type;
    private String imageUrl;
}
