package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import lombok.*;
import net.ikwa.techconnect.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    private String imageUrl;

    private int views = 0;
    private int sales = 0;
    private double rating = 0;
    private int reviews = 0;

    private LocalDate createdAt;

    // 🔥 LINK TO CREATOR
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private CreatorUserModel creator;
}
