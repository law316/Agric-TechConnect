package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.Transaction;
import net.ikwa.techconnect.service.PaymentService;
import net.ikwa.techconnect.userregDTO.PaymentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @PostMapping("/registration/{userId}/initialize")
    public ResponseEntity<?> initializeRegistrationPayment(
            @PathVariable Integer userId,
            @RequestParam BigDecimal amount
    ) {
        try {
            PaymentDTO response = paymentService.initializeRegistrationPayment(userId, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> paymentCallback(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam("tx_ref") String txRef,
            @RequestParam(value = "transaction_id", required = false) String transactionId
    ) {
        try {
            String normalizedStatus = status == null ? "" : status.trim().toLowerCase();

            boolean looksSuccessful =
                    "successful".equals(normalizedStatus) || "completed".equals(normalizedStatus);

            if (looksSuccessful && transactionId != null && !transactionId.isBlank()) {
                Transaction verified = paymentService.verifyAndProcessPayment(txRef, transactionId);

                String successUrl = frontendBaseUrl + "/payment/success?email=" +
                        URLEncoder.encode(verified.getUser().getEmail(), StandardCharsets.UTF_8) +
                        "&payment=success&tx_ref=" +
                        URLEncoder.encode(txRef, StandardCharsets.UTF_8);

                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(successUrl))
                        .build();
            }

            String pendingUrl = frontendBaseUrl + "/profile/promoter?payment=pending&tx_ref=" +
                    URLEncoder.encode(txRef, StandardCharsets.UTF_8) +
                    "&status=" + URLEncoder.encode(
                    normalizedStatus.isBlank() ? "unknown" : normalizedStatus,
                    StandardCharsets.UTF_8
            );

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(pendingUrl))
                    .build();

        } catch (Exception e) {
            String errorUrl = frontendBaseUrl + "/profile/promoter?payment=error&tx_ref=" +
                    URLEncoder.encode(txRef, StandardCharsets.UTF_8) +
                    "&message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(errorUrl))
                    .build();
        }
    }

    @PostMapping(
            value = "/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(value = "verif-hash", required = false) String verifHash,
            @RequestHeader(value = "flutterwave-signature", required = false) String flutterwaveSignature,
            @RequestBody String rawBody
    ) {
        try {
            paymentService.handleWebhook(verifHash, flutterwaveSignature, rawBody);
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
    @GetMapping("/registration/{userId}/status")
    public ResponseEntity<?> getRegistrationPaymentStatus(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(paymentService.getRegistrationPaymentStatus(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/admin/reconcile")
    public ResponseEntity<?> reconcilePendingPayment(@RequestParam String txRef) {
        try {
            Transaction tx = paymentService.reconcilePendingPayment(txRef);
            return ResponseEntity.ok(Map.of(
                    "message", "Payment reconciled successfully",
                    "txRef", tx.getTxRef(),
                    "status", tx.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingRegistrationPayments() {
        return ResponseEntity.ok(paymentService.getPendingRegistrationPayments());
    }

    @GetMapping("/admin/transactions")
    public ResponseEntity<?> getAllRegistrationPayments() {
        try {
            return ResponseEntity.ok(paymentService.getAllRegistrationPayments());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}