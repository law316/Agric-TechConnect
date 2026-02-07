package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

import net.ikwa.techconnect.model.PromoterUserModel;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private PromoterUserModel user;

    private BigDecimal amount;

    private String type; // commission, withdrawal, bonus, etc.

    private LocalDateTime date = LocalDateTime.now();

    private String status = "completed"; // pending/completed/failed
}
