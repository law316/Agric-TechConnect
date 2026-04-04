package net.ikwa.techconnect.service;

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
}