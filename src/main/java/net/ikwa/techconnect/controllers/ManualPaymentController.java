/*package net.ikwa.techconnect.controllers;

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
}*/
package net.ikwa.techconnect.controllers;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.service.ManualPaymentService;
import net.ikwa.techconnect.userregDTO.ManualPaymentRequestDTO;
import net.ikwa.techconnect.userregDTO.ManualPaymentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<ManualPaymentResponseDTO> getLatestManualPaymentByUserId(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(manualPaymentService.getLatestManualPaymentByUserId(userId));
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<ManualPaymentResponseDTO> updateManualPaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(
                manualPaymentService.updateManualPaymentStatus(paymentId, body.get("status"))
        );
    }
    @PatchMapping("/{paymentId}/confirm-paid")
    public ResponseEntity<ManualPaymentResponseDTO> confirmManualPaymentPaid(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(manualPaymentService.confirmManualPaymentPaid(paymentId));
    }

}
