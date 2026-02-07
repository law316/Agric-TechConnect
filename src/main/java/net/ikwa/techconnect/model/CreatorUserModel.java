package net.ikwa.techconnect.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.ikwa.techconnect.enums.ExperienceLevel;
import net.ikwa.techconnect.enums.Gender;
import net.ikwa.techconnect.enums.WorkPreference;

import java.math.BigDecimal;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreatorUserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String email;
    private String password;
    private String profileImage;
    private Gender gender;
    private String country;
    private String location;
    private String techField;
    private String specialization;
    private ExperienceLevel experience;
    private Integer projectsCompleted;
    private WorkPreference workPreference;
    private BigDecimal hourlyRate;
    @ElementCollection
    @CollectionTable(name = "creator_languages", joinColumns = @JoinColumn(name = "creator_id"))
    @Column(name = "language")
    private List<String> languages;
    private String bio;
    private String portfolioUrl;
    private String githubUrl;
    private String linkedinUrl;
    private String facebook;
    private String instagram;
    private String twitter;
    private String referredBy;
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Product> products;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getTechField() {
        return techField;
    }

    public void setTechField(String techField) {
        this.techField = techField;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public ExperienceLevel getExperience() {
        return experience;
    }

    public void setExperience(ExperienceLevel experience) {
        this.experience = experience;
    }

    public Integer getProjectsCompleted() {
        return projectsCompleted;
    }

    public void setProjectsCompleted(Integer projectsCompleted) {
        this.projectsCompleted = projectsCompleted;
    }

    public WorkPreference getWorkPreference() {
        return workPreference;
    }

    public void setWorkPreference(WorkPreference workPreference) {
        this.workPreference = workPreference;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getBio() {
        return bio;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
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

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }
}