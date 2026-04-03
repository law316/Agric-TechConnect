package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user making the payment / owner of this transaction
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private PromoterUserModel user;

    // Unique reference sent to Flutterwave
    @Column(nullable = false, unique = true)
    private String txRef;

    // Example: REGISTRATION_PAYMENT, PRODUCT_PAYMENT, SUBSCRIPTION_PAYMENT, WITHDRAWAL
    @Column(nullable = false)
    private String paymentType;

    // Optional: amount expected from Flutterwave
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedAmount;

    // Actual amount confirmed after verification
    @Column(precision = 19, scale = 2)
    private BigDecimal paidAmount;

    // NGN by default
    private String currency = "NGN";

    // PENDING, SUCCESS, FAILED
    private String status = "PENDING";

    // Flutterwave returned transaction ID
    private String flutterwaveTransactionId;

    // Optional: Flutterwave payment method or channel
    private String paymentMethod;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime verifiedAt;
}