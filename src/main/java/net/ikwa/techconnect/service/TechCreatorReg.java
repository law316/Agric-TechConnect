package net.ikwa.techconnect.service;

import net.ikwa.techconnect.model.CreatorUserModel;
import net.ikwa.techconnect.repo.TechRegRepo;
import net.ikwa.techconnect.userregDTO.CreatorRegDTO;
import net.ikwa.techconnect.userregDTO.CreatorRegDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TechCreatorReg {

    @Autowired
    private TechRegRepo repo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ REGISTER CREATOR
    public void registerCreator(CreatorRegDTO dto) {

        if (dto.getName() == null || dto.getEmail() == null || dto.getPassword() == null
                || dto.getCountry() == null || dto.getTechField() == null) {
            throw new IllegalArgumentException("Required fields missing");
        }

        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        CreatorUserModel creator = new CreatorUserModel();
        creator.setName(dto.getName());
        creator.setEmail(dto.getEmail());
        creator.setPassword(passwordEncoder.encode(dto.getPassword()));
        creator.setProfileImage(dto.getProfileImage());
        creator.setGender(dto.getGender());
        creator.setCountry(dto.getCountry());
        creator.setLocation(dto.getLocation());
        creator.setTechField(dto.getTechField());
        creator.setSpecialization(dto.getSpecialization());
        creator.setExperience(dto.getExperience());
        creator.setProjectsCompleted(dto.getProjectsCompleted());
        creator.setWorkPreference(dto.getWorkPreference());
        creator.setHourlyRate(dto.getHourlyRate());
        creator.setLanguages(dto.getLanguages() != null ? dto.getLanguages() : new ArrayList<>());
        creator.setBio(dto.getBio());
        creator.setPortfolioUrl(dto.getPortfolioUrl());
        creator.setGithubUrl(dto.getGithubUrl());
        creator.setLinkedinUrl(dto.getLinkedinUrl());
        creator.setFacebook(dto.getFacebook());
        creator.setInstagram(dto.getInstagram());
        creator.setTwitter(dto.getTwitter());
        creator.setReferredBy(dto.getReferredBy());

        repo.save(creator);
    }

    // ✅ GET CREATOR DETAILS
    public CreatorRegDTO getCreatorDetails(String email) {
        CreatorUserModel creator = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        CreatorRegDTO dto = new CreatorRegDTO();
        dto.setName(creator.getName());
        dto.setEmail(creator.getEmail());
        dto.setProfileImage(creator.getProfileImage());
        dto.setGender(creator.getGender());
        dto.setCountry(creator.getCountry());
        dto.setLocation(creator.getLocation());
        dto.setTechField(creator.getTechField());
        dto.setSpecialization(creator.getSpecialization());
        dto.setExperience(creator.getExperience());
        dto.setProjectsCompleted(creator.getProjectsCompleted());
        dto.setWorkPreference(creator.getWorkPreference());
        dto.setHourlyRate(creator.getHourlyRate());
        dto.setLanguages(creator.getLanguages());
        dto.setBio(creator.getBio());
        dto.setPortfolioUrl(creator.getPortfolioUrl());
        dto.setGithubUrl(creator.getGithubUrl());
        dto.setLinkedinUrl(creator.getLinkedinUrl());
        dto.setFacebook(creator.getFacebook());
        dto.setInstagram(creator.getInstagram());
        dto.setTwitter(creator.getTwitter());

        return dto;
    }

    // UPDATE IMAGE
    public void updateProfileImage(String email, String image) {
        CreatorUserModel creator = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        creator.setProfileImage(image);
        repo.save(creator);
    }

    public void deleteProfileImage(String email) {
        CreatorUserModel creator = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        creator.setProfileImage(null);
        repo.save(creator);
    }
}
