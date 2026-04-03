package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The promoter making the withdrawal
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private PromoterUserModel user;

    // Unique reference for this withdrawal
    @Column(nullable = false, unique = true)
    private String reference;

    // Amount user requested to withdraw
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Flat withdrawal fee
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    // Amount after fee
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    // Bank details
    @Column(nullable = false)
    private String bankCode;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    // Flutterwave transfer id will come later
    @Column
    private String flutterwaveTransferId;

    // PENDING, PROCESSING, SUCCESS, FAILED
    @Column(nullable = false)
    private String status;

    @Column(length = 500)
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}