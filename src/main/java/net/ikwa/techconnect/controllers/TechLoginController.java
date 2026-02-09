package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.CreatorUserModel;
import net.ikwa.techconnect.service.TechLoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tech")
@RequiredArgsConstructor
@CrossOrigin
public class TechLoginController {

    private final TechLoginService techLoginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");
            String password = request.get("password");

            CreatorUserModel creator =
                    techLoginService.authenticateCreator(email, password);

            // ✅ SUCCESS
            return ResponseEntity.ok(creator);

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
