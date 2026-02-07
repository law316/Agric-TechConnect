package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import lombok.*;
import net.ikwa.techconnect.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promoters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoterUserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String country;
    private String location;

    private String facebook;
    private String instagram;
    private String twitter;
    private String linkedinUrl;

    @Column(unique = true, nullable = false)
    private String referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by_id")
    private PromoterUserModel referredBy;

    @OneToMany(mappedBy = "referredBy")
    private List<PromoterUserModel> referrals;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Builder.Default
    private int totalReferrals = 0;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}