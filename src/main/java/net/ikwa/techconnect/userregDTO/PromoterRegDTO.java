package net.ikwa.techconnect.userregDTO;

import lombok.Getter;
import lombok.Setter;
import net.ikwa.techconnect.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PromoterRegDTO {

    // BASIC INFO
    private String name;
    private String email;
    private String password;
    private String profileImage;
    private Gender gender;
    private String country;
    private String location;

    // SOCIALS
    private String facebook;
    private String instagram;
    private String twitter;
    private String linkedinUrl;

    // REFERRAL INPUT (user enters code)
    private String referredByCode;

    // SYSTEM OUTPUT FIELDS
    private String referralCode;
    private int totalReferrals;
    private BigDecimal totalEarnings;
    private BigDecimal walletBalance;
    private boolean active;
    private LocalDateTime createdAt;

    // REFERRER INFO (safe display)
    private String referredByName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getReferredByCode() {
        return referredByCode;
    }

    public void setReferredByCode(String referredByCode) {
        this.referredByCode = referredByCode;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public int getTotalReferrals() {
        return totalReferrals;
    }

    public void setTotalReferrals(int totalReferrals) {
        this.totalReferrals = totalReferrals;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReferredByName() {
        return referredByName;
    }

    public void setReferredByName(String referredByName) {
        this.referredByName = referredByName;
    }
}
