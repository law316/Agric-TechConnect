package net.ikwa.techconnect.service;

import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.model.Transaction;
import net.ikwa.techconnect.repo.PromoterRegRepo;
import net.ikwa.techconnect.repo.TransactionRepo;
import net.ikwa.techconnect.userregDTO.PromoterRegDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PromoterReg {

    @Autowired
    private PromoterRegRepo repo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TransactionRepo transactionRepo;

    // =============================
    // REGISTER NEW PROMOTER
    // =============================
    public void registerPromoter(PromoterRegDTO dto) {

        if (dto.getName() == null || dto.getName().isEmpty()
                || dto.getEmail() == null || dto.getEmail().isEmpty()
                || dto.getPassword() == null || dto.getPassword().isEmpty()
                || dto.getCountry() == null || dto.getCountry().isEmpty()
                || dto.getLocation() == null || dto.getLocation().isEmpty()) {
            throw new IllegalArgumentException("Required fields are missing");
        }

        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        String referralCode = generateReferralCode(dto.getName());
        while (repo.findByReferralCode(referralCode).isPresent()) {
            referralCode = generateReferralCode(dto.getName());
        }

        PromoterUserModel promoter = new PromoterUserModel();
        promoter.setName(dto.getName());
        promoter.setEmail(dto.getEmail());
        promoter.setPassword(encodedPassword);
        promoter.setProfileImage(dto.getProfileImage());
        promoter.setGender(dto.getGender());
        promoter.setCountry(dto.getCountry());
        promoter.setLocation(dto.getLocation());
        promoter.setFacebook(dto.getFacebook());
        promoter.setInstagram(dto.getInstagram());
        promoter.setTwitter(dto.getTwitter());
        promoter.setLinkedinUrl(dto.getLinkedinUrl());

        promoter.setReferralCode(referralCode);
        promoter.setActive(true);
        promoter.setTotalEarnings(BigDecimal.ZERO);
        promoter.setWalletBalance(BigDecimal.ZERO);
        promoter.setCreatedAt(LocalDateTime.now());

        if (dto.getReferredByCode() != null && !dto.getReferredByCode().isEmpty()) {
            PromoterUserModel referrer = repo.findByReferralCode(dto.getReferredByCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid referral code"));
            promoter.setReferredBy(referrer);

            // Update referrer's stats
            referrer.setTotalReferrals(referrer.getTotalReferrals() + 1);
            repo.save(referrer);
        }

        repo.save(promoter);
    }

    // =============================
    // PROCESS PAYMENT WITH COMMISSION
    // =============================
    public void processPayment(String buyerEmail, BigDecimal amountPaid) {
        // Find buyer
        PromoterUserModel buyer = repo.findByEmail(buyerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found"));

        // Handle referrer commission (only once per payment)
        PromoterUserModel referrer = buyer.getReferredBy();
        if (referrer != null) {
            BigDecimal commission = amountPaid.multiply(BigDecimal.valueOf(0.8));

            // Update referrer's wallet & total earnings
            referrer.setWalletBalance(referrer.getWalletBalance().add(commission));
            referrer.setTotalEarnings(referrer.getTotalEarnings().add(commission));
            repo.save(referrer);

            // Record commission transaction
            Transaction transaction = Transaction.builder()
                    .user(referrer)
                    .amount(commission)
                    .type("commission")
                    .status("completed")
                    .date(LocalDateTime.now())
                    .build();
            transactionRepo.save(transaction);
        }

        // System retains 20%
        BigDecimal systemShare = amountPaid.multiply(BigDecimal.valueOf(0.2));
        // Optional: store systemShare in a system table if you want
    }

    // =============================
    // GET PROMOTER DETAILS
    // =============================
    public PromoterRegDTO getPromoterDetails(String email) {
        PromoterUserModel promoter = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Promoter not found"));

        PromoterRegDTO dto = new PromoterRegDTO();
        dto.setName(promoter.getName());
        dto.setEmail(promoter.getEmail());
        dto.setProfileImage(promoter.getProfileImage());
        dto.setGender(promoter.getGender());
        dto.setCountry(promoter.getCountry());
        dto.setLocation(promoter.getLocation());
        dto.setFacebook(promoter.getFacebook());
        dto.setInstagram(promoter.getInstagram());
        dto.setTwitter(promoter.getTwitter());
        dto.setLinkedinUrl(promoter.getLinkedinUrl());
        dto.setReferralCode(promoter.getReferralCode());
        dto.setTotalReferrals(promoter.getTotalReferrals());
        dto.setTotalEarnings(promoter.getTotalEarnings());
        dto.setWalletBalance(promoter.getWalletBalance());
        dto.setActive(promoter.isActive());
        dto.setCreatedAt(promoter.getCreatedAt());
        if (promoter.getReferredBy() != null) {
            dto.setReferredByName(promoter.getReferredBy().getName());
        }
        return dto;
    }

    // =============================
    // UPDATE PROFILE IMAGE
    // =============================
    public void updateProfileImage(String email, String image) {
        PromoterUserModel promoter = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Promoter not found"));
        promoter.setProfileImage(image);
        repo.save(promoter);
    }

    // =============================
    // DELETE PROFILE IMAGE
    // =============================
    public void deleteProfileImage(String email) {
        PromoterUserModel promoter = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Promoter not found"));
        promoter.setProfileImage(null);
        repo.save(promoter);
    }

    // =============================
    // UPDATE BASIC PROFILE
    // =============================
    public PromoterRegDTO updateBasicProfile(
            String email,
            String name,
            String country,
            String location,
            String facebook,
            String instagram,
            String twitter,
            String linkedinUrl
    ) {
        PromoterUserModel promoter = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Promoter not found"));

        if (name != null) promoter.setName(name);
        if (country != null) promoter.setCountry(country);
        if (location != null) promoter.setLocation(location);
        if (facebook != null) promoter.setFacebook(facebook);
        if (instagram != null) promoter.setInstagram(instagram);
        if (twitter != null) promoter.setTwitter(twitter);
        if (linkedinUrl != null) promoter.setLinkedinUrl(linkedinUrl);

        repo.save(promoter);

        return getPromoterDetails(email);
    }

    // =============================
    // HELPER: GENERATE REFERRAL CODE
    // =============================
    private String generateReferralCode(String name) {
        String base = name.replaceAll("\\s+", "").toLowerCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 5);
        return base + randomPart;
    }
}
