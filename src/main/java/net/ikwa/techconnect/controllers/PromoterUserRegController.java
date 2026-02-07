package net.ikwa.techconnect.controllers;

import net.ikwa.techconnect.model.Transaction;
import net.ikwa.techconnect.userregDTO.PromoterRegDTO;
import net.ikwa.techconnect.service.PromoterReg;
import net.ikwa.techconnect.repo.TransactionRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promoters")
@CrossOrigin("*")
public class PromoterUserRegController {

    @Autowired
    private PromoterReg promoterService;

    @Autowired
    private TransactionRepo transactionRepo;

    // =============================
    // REGISTER PROMOTER
    // =============================
    @PostMapping("/register")
    public ResponseEntity<?> registerPromoter(@RequestBody PromoterRegDTO dto) {
        try {
            promoterService.registerPromoter(dto);
            return ResponseEntity.ok("Promoter registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============================
    // GET PROMOTER DETAILS
    // =============================
    @GetMapping("/{email}")
    public ResponseEntity<?> getPromoter(@PathVariable String email) {
        try {
            PromoterRegDTO promoter = promoterService.getPromoterDetails(email);
            return ResponseEntity.ok(promoter);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Promoter not found");
        }
    }

    // =============================
    // UPDATE PROFILE IMAGE
    // =============================
    @PutMapping("/{email}/image")
    public ResponseEntity<?> updateImage(
            @PathVariable String email,
            @RequestBody Map<String, String> body) {
        try {
            promoterService.updateProfileImage(email, body.get("profileImage"));
            return ResponseEntity.ok("Image updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============================
    // DELETE PROFILE IMAGE
    // =============================
    @DeleteMapping("/{email}/image")
    public ResponseEntity<?> deleteImage(@PathVariable String email) {
        try {
            promoterService.deleteProfileImage(email);
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============================
    // UPDATE BASIC PROFILE
    // =============================
    @PutMapping("/{email}")
    public ResponseEntity<?> updatePromoter(
            @PathVariable String email,
            @RequestBody Map<String, String> body) {
        try {
            PromoterRegDTO updated = promoterService.updateBasicProfile(
                    email,
                    body.get("name"),
                    body.get("country"),
                    body.get("location"),
                    body.get("facebook"),
                    body.get("instagram"),
                    body.get("twitter"),
                    body.get("linkedinUrl")
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============================
    // PROCESS PAYMENT
    // =============================
    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(
            @RequestParam String buyerEmail,
            @RequestParam BigDecimal amount) {
        try {
            promoterService.processPayment(buyerEmail, amount);
            return ResponseEntity.ok("Payment processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =============================
    // GET TRANSACTION HISTORY
    // =============================
    @GetMapping("/{email}/transactions")
    public ResponseEntity<?> getTransactionHistory(@PathVariable String email) {
        try {
            List<Transaction> transactions = transactionRepo.findByUserEmail(email);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
