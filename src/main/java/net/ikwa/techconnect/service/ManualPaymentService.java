/*package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.ManualPaymentModel;
import net.ikwa.techconnect.repo.ManualPaymentRepo;
import net.ikwa.techconnect.userregDTO.ManualPaymentRequestDTO;
import net.ikwa.techconnect.userregDTO.ManualPaymentResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualPaymentService {

    private final ManualPaymentRepo manualPaymentRepo;

    public ManualPaymentResponseDTO createManualPayment(ManualPaymentRequestDTO request) {

        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        if (request.getBankName() == null || request.getBankName().trim().isEmpty()) {
            throw new RuntimeException("Bank name is required");
        }

        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }

        if (request.getAccountName() == null || request.getAccountName().trim().isEmpty()) {
            throw new RuntimeException("Account name is required");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        ManualPaymentModel payment = ManualPaymentModel.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName().trim())
                .bankName(request.getBankName().trim())
                .accountNumber(request.getAccountNumber().trim())
                .accountName(request.getAccountName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim())
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .build();

        ManualPaymentModel savedPayment = manualPaymentRepo.save(payment);

        return mapToResponse(savedPayment, "Manual withdrawal request submitted successfully");
    }

    public List<ManualPaymentResponseDTO> getAllManualPayments() {
        return manualPaymentRepo.findAll()
                .stream()
                .map(payment -> mapToResponse(payment, null))
                .collect(Collectors.toList());
    }

    public List<ManualPaymentResponseDTO> getManualPaymentsByUserId(Integer userId) {
        return manualPaymentRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(payment -> mapToResponse(payment, null))
                .collect(Collectors.toList());
    }

    private ManualPaymentResponseDTO mapToResponse(ManualPaymentModel payment, String message) {
        return ManualPaymentResponseDTO.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .fullName(payment.getFullName())
                .bankName(payment.getBankName())
                .accountNumber(payment.getAccountNumber())
                .accountName(payment.getAccountName())
                .phoneNumber(payment.getPhoneNumber())
                .email(payment.getEmail())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .message(message)
                .build();
    }
}*/
package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.ManualPaymentModel;
import net.ikwa.techconnect.repo.ManualPaymentRepo;
import net.ikwa.techconnect.repo.PromoterRegRepo;
import net.ikwa.techconnect.userregDTO.ManualPaymentRequestDTO;
import net.ikwa.techconnect.userregDTO.ManualPaymentResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.repo.TechRegRepo;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ManualPaymentService {

    private final ManualPaymentRepo manualPaymentRepo;
    private final PromoterRegRepo promoterRegRepo;

    public ManualPaymentResponseDTO createManualPayment(ManualPaymentRequestDTO request) {

        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }

        if (request.getBankName() == null || request.getBankName().trim().isEmpty()) {
            throw new RuntimeException("Bank name is required");
        }

        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }

        if (request.getAccountName() == null || request.getAccountName().trim().isEmpty()) {
            throw new RuntimeException("Account name is required");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        boolean hasPendingRequest = manualPaymentRepo.existsByUserIdAndStatus(request.getUserId(), "PENDING");
        if (hasPendingRequest) {
            throw new RuntimeException("You already have a pending withdrawal request. Wait until it is confirmed.");
        }

        ManualPaymentModel payment = ManualPaymentModel.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName().trim())
                .bankName(request.getBankName().trim())
                .accountNumber(request.getAccountNumber().trim())
                .accountName(request.getAccountName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim())
                .amount(request.getAmount())
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        ManualPaymentModel savedPayment = manualPaymentRepo.save(payment);

        return mapToResponse(savedPayment, "Manual withdrawal request submitted successfully");
    }

    public List<ManualPaymentResponseDTO> getAllManualPayments() {
        return manualPaymentRepo.findAll()
                .stream()
                .map(payment -> mapToResponse(payment, null))
                .collect(Collectors.toList());
    }

    public List<ManualPaymentResponseDTO> getManualPaymentsByUserId(Integer userId) {
        return manualPaymentRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(payment -> mapToResponse(payment, null))
                .collect(Collectors.toList());
    }

    public ManualPaymentResponseDTO getLatestManualPaymentByUserId(Integer userId) {
        ManualPaymentModel payment = manualPaymentRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No manual withdrawal request found for this user"));

        return mapToResponse(payment, null);
    }

    public ManualPaymentResponseDTO updateManualPaymentStatus(Long paymentId, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals("PENDING") && !normalizedStatus.equals("SUCCESS")) {
            throw new RuntimeException("Status must be PENDING or SUCCESS");
        }

        ManualPaymentModel payment = manualPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Manual payment request not found"));

        payment.setStatus(normalizedStatus);

        ManualPaymentModel saved = manualPaymentRepo.save(payment);

        return mapToResponse(saved, "Manual withdrawal status updated successfully");
    }

    private ManualPaymentResponseDTO mapToResponse(ManualPaymentModel payment, String message) {
        return ManualPaymentResponseDTO.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .fullName(payment.getFullName())
                .bankName(payment.getBankName())
                .accountNumber(payment.getAccountNumber())
                .accountName(payment.getAccountName())
                .phoneNumber(payment.getPhoneNumber())
                .email(payment.getEmail())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .status(payment.getStatus())
                .message(message)
                .build();
    }
    @Transactional
    public ManualPaymentResponseDTO confirmManualPaymentPaid(Long paymentId) {
        ManualPaymentModel payment = manualPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Manual payment request not found"));

        if (payment.getStatus() == null || !payment.getStatus().equalsIgnoreCase("PENDING")) {
            throw new RuntimeException("Only pending manual payment requests can be confirmed");
        }

        PromoterUserModel user = promoterRegRepo.findById(payment.getUserId())
                .orElseThrow(() -> new RuntimeException("Promoter user not found"));

        BigDecimal currentCommissionBalance = user.getCommissionBalance() == null
                ? BigDecimal.ZERO
                : user.getCommissionBalance();

        BigDecimal paymentAmount = payment.getAmount() == null
                ? BigDecimal.ZERO
                : payment.getAmount();

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Manual payment amount is invalid");
        }

        if (currentCommissionBalance.compareTo(paymentAmount) < 0) {
            throw new RuntimeException("User commission balance is less than the withdrawal amount");
        }

        user.setCommissionBalance(currentCommissionBalance.subtract(paymentAmount));
        promoterRegRepo.save(user);

        payment.setStatus("SUCCESS");
        ManualPaymentModel savedPayment = manualPaymentRepo.save(payment);

        return mapToResponse(savedPayment, "Manual payment confirmed successfully and commission balance updated");
    }
}