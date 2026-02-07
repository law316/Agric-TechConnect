package net.ikwa.techconnect.userregDTO;



import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String type;
    private String imageUrl;
    private int views;
    private int sales;
    private double rating;
    private int reviews;
    private String createdAt;
}
