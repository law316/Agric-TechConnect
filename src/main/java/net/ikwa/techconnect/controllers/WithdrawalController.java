package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.service.WithdrawalService;
import net.ikwa.techconnect.userregDTO.WithdrawalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/withdrawals")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/request/{userId}")
    public ResponseEntity<?> requestWithdrawal(
            @PathVariable Integer userId,
            @RequestBody WithdrawalRequest request
    ) {
        try {
            return ResponseEntity.ok(withdrawalService.requestWithdrawal(userId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getWithdrawalHistory(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(withdrawalService.getUserWithdrawals(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
    @PostMapping("/initiate/{withdrawalId}")
    public ResponseEntity<?> initiateWithdrawalTransfer(@PathVariable Long withdrawalId) {
        try {
            return ResponseEntity.ok(withdrawalService.initiateWithdrawalTransfer(withdrawalId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
    @PostMapping("/verify/{withdrawalId}")
    public ResponseEntity<?> verifyWithdrawalTransfer(@PathVariable Long withdrawalId) {
        try {
            return ResponseEntity.ok(withdrawalService.verifyWithdrawalTransfer(withdrawalId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
    @PostMapping(
            value = "/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> handleWithdrawalWebhook(
            @RequestHeader(value = "verif-hash", required = false) String verifHash,
            @RequestHeader(value = "flutterwave-signature", required = false) String flutterwaveSignature,
            @RequestBody String rawBody
    ) {
        try {
            withdrawalService.handleWithdrawalWebhook(verifHash, flutterwaveSignature, rawBody);
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "ignored",
                    "reason", e.getMessage()
            ));
        }
    }
}