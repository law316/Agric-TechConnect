package net.ikwa.techconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.model.Withdrawal;
import net.ikwa.techconnect.repo.PromoterRegRepo;
import net.ikwa.techconnect.repo.WithdrawalRepo;
import net.ikwa.techconnect.userregDTO.WithdrawalRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final PromoterRegRepo userRepository;
    private final WithdrawalRepo withdrawalRepo;

    @Value("${flutterwave.secret-key}")
    private String flutterwaveSecretKey;

    @Value("${flutterwave.base-url}")
    private String flutterwaveBaseUrl;

    @Value("${flutterwave.transfer-callback-url}")
    private String transferCallbackUrl;

    @Value("${flutterwave.transfer-currency}")
    private String transferCurrency;

    @Value("${flutterwave.secret-hash}")
    private String flutterwaveSecretHash;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> requestWithdrawal(Integer userId, WithdrawalRequest request) {

        PromoterUserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasOpenWithdrawal = withdrawalRepo.existsByUserIdAndStatusIn(
                userId,
                List.of("PENDING", "PROCESSING")
        );

        if (hasOpenWithdrawal) {
            throw new RuntimeException("You already have a pending withdrawal request");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than zero");
        }

        if (request.getBankCode() == null || request.getBankCode().isBlank()) {
            throw new RuntimeException("Bank code is required");
        }

        if (request.getAccountNumber() == null || request.getAccountNumber().isBlank()) {
            throw new RuntimeException("Account number is required");
        }

        if (request.getAccountName() == null || request.getAccountName().isBlank()) {
            throw new RuntimeException("Account name is required");
        }

        BigDecimal currentBalance = user.getCommissionBalance() == null
                ? BigDecimal.ZERO
                : user.getCommissionBalance();

        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        String reference = "WD-" + userId + "-" + UUID.randomUUID();

        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .reference(reference)
                .amount(request.getAmount())
                .fee(BigDecimal.ZERO)
                .netAmount(request.getAmount())
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .status("PENDING")
                .failureReason(null)
                .createdAt(LocalDateTime.now())
                .processedAt(null)
                .build();

        withdrawalRepo.save(withdrawal);

        return Map.of(
                "message", "Withdrawal request saved successfully",
                "reference", reference,
                "status", "PENDING",
                "amount", withdrawal.getAmount()
        );
    }

    public List<Withdrawal> getUserWithdrawals(Integer userId) {
        return withdrawalRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> initiateWithdrawalTransfer(Long withdrawalId) {

        Withdrawal withdrawal = withdrawalRepo.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));

        if (!"PENDING".equalsIgnoreCase(withdrawal.getStatus())) {
            throw new RuntimeException("Only pending withdrawals can be initiated");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(flutterwaveSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("account_bank", withdrawal.getBankCode());
        payload.put("account_number", withdrawal.getAccountNumber());
        payload.put("amount", withdrawal.getNetAmount());
        payload.put("narration", "Promoter withdrawal");
        payload.put("currency", transferCurrency);
        payload.put("reference", withdrawal.getReference());
        payload.put("callback_url", transferCallbackUrl);
        payload.put("debit_currency", transferCurrency);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                flutterwaveBaseUrl + "/v3/transfers",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();

        if (responseBody == null) {
            throw new RuntimeException("Empty response from Flutterwave");
        }

        Object statusObj = responseBody.get("status");
        String apiStatus = statusObj == null ? "" : statusObj.toString();

        if (!"success".equalsIgnoreCase(apiStatus)) {
            throw new RuntimeException("Flutterwave transfer initiation failed");
        }

        Map data = (Map) responseBody.get("data");
        if (data != null && data.get("id") != null) {
            withdrawal.setFlutterwaveTransferId(String.valueOf(data.get("id")));
        }

        withdrawal.setStatus("PROCESSING");
        withdrawalRepo.save(withdrawal);

        return Map.of(
                "message", "Withdrawal transfer initiated",
                "withdrawalId", withdrawal.getId(),
                "reference", withdrawal.getReference(),
                "status", withdrawal.getStatus()
        );
    }

    public Map<String, Object> verifyWithdrawalTransfer(Long withdrawalId) {

        Withdrawal withdrawal = withdrawalRepo.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));

        if (withdrawal.getFlutterwaveTransferId() == null || withdrawal.getFlutterwaveTransferId().isBlank()) {
            throw new RuntimeException("No Flutterwave transfer id found for this withdrawal");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(flutterwaveSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                flutterwaveBaseUrl + "/v3/transfers/" + withdrawal.getFlutterwaveTransferId(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Empty response from Flutterwave");
        }

        Object statusObj = responseBody.get("status");
        String apiStatus = statusObj == null ? "" : statusObj.toString();

        if (!"success".equalsIgnoreCase(apiStatus)) {
            throw new RuntimeException("Flutterwave transfer verification failed");
        }

        Map data = (Map) responseBody.get("data");
        String transferStatus = data != null && data.get("status") != null
                ? data.get("status").toString()
                : "";

        if ("SUCCESSFUL".equalsIgnoreCase(transferStatus)) {
            PromoterUserModel user = withdrawal.getUser();

            if (!"SUCCESS".equalsIgnoreCase(withdrawal.getStatus())) {
                if (user.getCommissionBalance() == null) {
                    user.setCommissionBalance(BigDecimal.ZERO);
                }

                user.setCommissionBalance(user.getCommissionBalance().subtract(withdrawal.getAmount()));
                userRepository.save(user);
            }

            withdrawal.setStatus("SUCCESS");
            withdrawal.setFailureReason(null);
            withdrawal.setProcessedAt(LocalDateTime.now());
            withdrawalRepo.save(withdrawal);

            return Map.of(
                    "message", "Withdrawal successful",
                    "withdrawalId", withdrawal.getId(),
                    "status", withdrawal.getStatus(),
                    "transferStatus", transferStatus
            );
        }

        if ("FAILED".equalsIgnoreCase(transferStatus)) {
            withdrawal.setStatus("FAILED");
            withdrawal.setFailureReason("Flutterwave transfer failed");
            withdrawal.setProcessedAt(LocalDateTime.now());
            withdrawalRepo.save(withdrawal);

            return Map.of(
                    "message", "Withdrawal failed",
                    "withdrawalId", withdrawal.getId(),
                    "status", withdrawal.getStatus(),
                    "transferStatus", transferStatus
            );
        }

        withdrawal.setStatus("PROCESSING");
        withdrawalRepo.save(withdrawal);

        return Map.of(
                "message", "Withdrawal still processing",
                "withdrawalId", withdrawal.getId(),
                "status", withdrawal.getStatus(),
                "transferStatus", transferStatus
        );
    }

    public void handleWithdrawalWebhook(String verifHash, String flutterwaveSignature, String rawBody) {

        boolean validHash = verifHash != null
                && flutterwaveSecretHash != null
                && !flutterwaveSecretHash.isBlank()
                && flutterwaveSecretHash.equals(verifHash);

        boolean validSignature = flutterwaveSignature != null
                && flutterwaveSecretHash != null
                && !flutterwaveSecretHash.isBlank()
                && flutterwaveSecretHash.equals(flutterwaveSignature);

        if (!validHash && !validSignature) {
            throw new SecurityException("Invalid webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode data = root.path("data");

            String reference = data.path("reference").asText(null);
            String transferId = data.path("id").asText(null);
            String status = data.path("status").asText(null);
            String completeMessage = data.path("complete_message").asText(null);

            if (reference == null || reference.isBlank()) {
                throw new RuntimeException("Webhook missing withdrawal reference");
            }

            Withdrawal withdrawal = withdrawalRepo.findByReference(reference)
                    .orElseThrow(() -> new RuntimeException("Withdrawal not found for reference: " + reference));

            if (transferId != null && !transferId.isBlank()) {
                withdrawal.setFlutterwaveTransferId(transferId);
            }

            if ("SUCCESSFUL".equalsIgnoreCase(status)) {
                PromoterUserModel user = withdrawal.getUser();

                if (!"SUCCESS".equalsIgnoreCase(withdrawal.getStatus())) {
                    if (user.getCommissionBalance() == null) {
                        user.setCommissionBalance(BigDecimal.ZERO);
                    }

                    user.setCommissionBalance(user.getCommissionBalance().subtract(withdrawal.getAmount()));
                    userRepository.save(user);
                }

                withdrawal.setStatus("SUCCESS");
                withdrawal.setFailureReason(null);
                withdrawal.setProcessedAt(LocalDateTime.now());
                withdrawalRepo.save(withdrawal);
                return;
            }

            if ("FAILED".equalsIgnoreCase(status)) {
                withdrawal.setStatus("FAILED");
                withdrawal.setFailureReason(
                        completeMessage == null || completeMessage.isBlank()
                                ? "Flutterwave transfer failed"
                                : completeMessage
                );
                withdrawal.setProcessedAt(LocalDateTime.now());
                withdrawalRepo.save(withdrawal);
                return;
            }

            withdrawal.setStatus("PROCESSING");
            withdrawalRepo.save(withdrawal);

        } catch (Exception e) {
            throw new RuntimeException("Invalid withdrawal webhook payload", e);
        }
    }
}