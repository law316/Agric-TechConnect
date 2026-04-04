package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.service.ManualPaymentService;
import net.ikwa.techconnect.userregDTO.ManualPaymentRequestDTO;
import net.ikwa.techconnect.userregDTO.ManualPaymentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manual-payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ManualPaymentController {

    private final ManualPaymentService manualPaymentService;

    @PostMapping("/request")
    public ResponseEntity<ManualPaymentResponseDTO> createManualPayment(
            @RequestBody ManualPaymentRequestDTO request
    ) {
        return ResponseEntity.ok(manualPaymentService.createManualPayment(request));
    }

    @GetMapping
    public ResponseEntity<List<ManualPaymentResponseDTO>> getAllManualPayments() {
        return ResponseEntity.ok(manualPaymentService.getAllManualPayments());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ManualPaymentResponseDTO>> getManualPaymentsByUserId(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(manualPaymentService.getManualPaymentsByUserId(userId));
    }
}