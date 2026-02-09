package net.ikwa.techconnect.controllers;

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
}
