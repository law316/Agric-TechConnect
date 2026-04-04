/*package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.service.PromoterLoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/promoter")
@RequiredArgsConstructor
@CrossOrigin
public class PromoterLoginController {

    private final PromoterLoginService promoterLoginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");
            String password = request.get("password");

            PromoterUserModel promoter =
                    promoterLoginService.authenticatePromoter(email, password);

            // ✅ SUCCESS → frontend redirects
            return ResponseEntity.ok(promoter);

        } catch (IllegalArgumentException e) {
            // ❌ INVALID CREDENTIALS
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            // ❌ UNEXPECTED ERROR
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong. Please try again.");
        }
    }
}*/
package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.service.PromoterLoginService;
import net.ikwa.techconnect.userregDTO.PromoterRegDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/promoter")
@RequiredArgsConstructor
@CrossOrigin
public class PromoterLoginController {

    private final PromoterLoginService promoterLoginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            PromoterUserModel promoter =
                    promoterLoginService.authenticatePromoter(email, password);

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
            dto.setId(promoter.getId());
            dto.setPaymentVerified(Boolean.TRUE.equals(promoter.getPaymentVerified()));

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong. Please try again.");
        }
    }
}