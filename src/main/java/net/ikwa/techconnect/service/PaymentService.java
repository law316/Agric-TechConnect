package net.ikwa.techconnect.service;

import lombok.RequiredArgsConstructor;
import net.ikwa.techconnect.model.PromoterUserModel;
import net.ikwa.techconnect.model.Transaction;
import net.ikwa.techconnect.repo.PromoterRegRepo;
import net.ikwa.techconnect.repo.TechRegRepo;
import net.ikwa.techconnect.repo.TransactionRepo;
import net.ikwa.techconnect.userregDTO.PaymentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepo transactionRepo;
    private final PromoterRegRepo userRepository;

    @Value("${flutterwave.secret-key}")
    private String flutterwaveSecretKey;

    @Value("${flutterwave.secret-hash}")
    private String flutterwaveSecretHash;

    @Value("${flutterwave.base-url}")
    private String flutterwaveBaseUrl;

    @Value("${flutterwave.redirect-url}")
    private String flutterwaveRedirectUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentDTO initializeRegistrationPayment(Integer userId, BigDecimal amount) {
        PromoterUserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid payment amount");
        }

        String txRef = "REG-" + userId + "-" + UUID.randomUUID();

        Transaction tx = Transaction.builder()
                .user(user)
                .txRef(txRef)
                .paymentType("REGISTRATION_PAYMENT")
                .expectedAmount(amount)
                .paidAmount(BigDecimal.ZERO)
                .currency("NGN")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tx_ref", txRef);
        payload.put("amount", amount);
        payload.put("currency", "NGN");
        payload.put("redirect_url", flutterwaveRedirectUrl);

        Map<String, Object> customer = new HashMap<>();
        customer.put("email", user.getEmail() != null ? user.getEmail() : "customer@example.com");
        customer.put("name", user.getName() != null ? user.getName() : "Customer");
        customer.put("phonenumber", user.getPhone() != null ? user.getPhone() : "");
        payload.put("customer", customer);

        Map<String, Object> customizations = new HashMap<>();
        customizations.put("title", "Registration Payment");
        customizations.put("description", "Payment for account activation");
        payload.put("customizations", customizations);

        Map<String, Object> meta = new HashMap<>();
        meta.put("paymentType", "REGISTRATION_PAYMENT");
        meta.put("userId", user.getId());
        payload.put("meta", meta);

        String paymentLink = createFlutterwavePayment(payload);

        return new PaymentDTO(txRef, paymentLink, "PENDING");
    }

    public Transaction getTransactionByTxRef(String txRef) {
        return transactionRepo.findByTxRef(txRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public Transaction markTransactionSuccessful(String txRef, String flutterwaveTransactionId) {
        return verifyAndProcessPayment(txRef, flutterwaveTransactionId);
    }

    public Transaction verifyAndProcessPayment(String txRef, String flutterwaveTransactionId) {
        Transaction existingByFlwId = null;
        if (flutterwaveTransactionId != null && !flutterwaveTransactionId.isBlank()) {
            existingByFlwId = transactionRepo.findByFlutterwaveTransactionId(flutterwaveTransactionId).orElse(null);
        }

        if (existingByFlwId != null && "SUCCESS".equalsIgnoreCase(existingByFlwId.getStatus())) {
            return existingByFlwId;
        }

        Transaction tx = transactionRepo.findByTxRef(txRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            return tx;
        }

        Map verification = verifyTransactionWithFlutterwave(flutterwaveTransactionId);

        Object dataObj = verification.get("data");
        if (!(dataObj instanceof Map)) {
            throw new RuntimeException("Invalid verification response from Flutterwave");
        }

        Map data = (Map) dataObj;

        String verifiedTxRef = data.get("tx_ref") == null ? null : data.get("tx_ref").toString();
        String verifiedStatus = data.get("status") == null ? null : data.get("status").toString();
        String verifiedCurrency = data.get("currency") == null ? null : data.get("currency").toString();
        BigDecimal verifiedAmount = toBigDecimal(data.get("amount"));

        if (verifiedTxRef == null || !verifiedTxRef.equals(txRef)) {
            throw new RuntimeException("Verified tx_ref does not match local transaction");
        }

        if (verifiedStatus == null || !"successful".equalsIgnoreCase(verifiedStatus)) {
            throw new RuntimeException("Payment is not successful on Flutterwave");
        }

        if (verifiedCurrency == null || !verifiedCurrency.equalsIgnoreCase(tx.getCurrency())) {
            throw new RuntimeException("Payment currency mismatch");
        }

        if (verifiedAmount == null || verifiedAmount.compareTo(tx.getExpectedAmount()) < 0) {
            throw new RuntimeException("Paid amount is less than expected amount");
        }

        tx.setStatus("SUCCESS");
        tx.setFlutterwaveTransactionId(flutterwaveTransactionId);
        tx.setPaidAmount(verifiedAmount);
        tx.setVerifiedAt(LocalDateTime.now());

        Transaction savedTx = transactionRepo.save(tx);

        applyBusinessSuccess(savedTx);

        return savedTx;
    }

    public void handleWebhook(String verifHash, String flutterwaveSignature, String rawBody) {
        if (!isValidWebhook(verifHash, flutterwaveSignature, rawBody)) {
            throw new SecurityException("Invalid webhook signature");
        }

        String txRef = extractJsonValue(rawBody, "tx_ref");
        String transactionId = extractJsonValue(rawBody, "id");
        String status = extractJsonValue(rawBody, "status");

        if (txRef == null || txRef.isBlank() || transactionId == null || transactionId.isBlank()) {
            throw new RuntimeException("Webhook missing tx_ref or transaction id");
        }

        if (!"successful".equalsIgnoreCase(status)) {
            throw new RuntimeException("Ignoring non-success webhook");
        }

        verifyAndProcessPayment(txRef, transactionId);
    }

    /*private void applyBusinessSuccess(Transaction savedTx) {
        if (!"REGISTRATION_PAYMENT".equalsIgnoreCase(savedTx.getPaymentType())) {
            return;
        }

        PromoterUserModel paidUser = userRepository.findById(savedTx.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Paid user not found"));

        BigDecimal paidAmount = savedTx.getPaidAmount();
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Paid amount is invalid");
        }

        BigDecimal referrerCommission = paidAmount.multiply(new BigDecimal("0.70"));
        BigDecimal systemShare = paidAmount.multiply(new BigDecimal("0.30"));

        // IMPORTANT:
        // Change referredBy to the correct field in your PromoterUserModel.
        // Example: paidUser.getReferredBy(), paidUser.getReferrer(), or paidUser.getSponsor()

        PromoterUserModel referrer = paidUser.getReferredBy();

        if (referrer != null) {
            BigDecimal currentCommission = referrer.getCommissionBalance() == null
                    ? BigDecimal.ZERO
                    : referrer.getCommissionBalance();

            referrer.setCommissionBalance(currentCommission.add(referrerCommission));
            userRepository.save(referrer);
        }

        BigDecimal currentSystemShare = paidUser.getSystemShareRecorded() == null
                ? BigDecimal.ZERO
                : paidUser.getSystemShareRecorded();

        paidUser.setSystemShareRecorded(currentSystemShare.add(systemShare));

        // optional: activate paid user after successful registration payment
        paidUser.setPaymentVerified(true);

        userRepository.save(paidUser);
    }*/
    private void applyBusinessSuccess(Transaction savedTx) {
        if (!"REGISTRATION_PAYMENT".equalsIgnoreCase(savedTx.getPaymentType())) {
            return;
        }

        PromoterUserModel paidUser = userRepository.findById(savedTx.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Paid user not found"));

        BigDecimal paidAmount = savedTx.getPaidAmount();
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Paid amount is invalid");
        }

        BigDecimal directCommission = paidAmount
                .multiply(new BigDecimal("0.70"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal systemShare = paidAmount
                .multiply(new BigDecimal("0.30"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // The person who directly referred the paid user
        PromoterUserModel directReferrer = paidUser.getReferredBy();

        if (directReferrer != null) {
            BigDecimal currentCommission = directReferrer.getCommissionBalance() == null
                    ? BigDecimal.ZERO
                    : directReferrer.getCommissionBalance();

            // Rule 1: direct referrer gets 70%
            directReferrer.setCommissionBalance(currentCommission.add(directCommission));
            userRepository.save(directReferrer);

            // Rule 2: direct referrer's own parent gets 15% of what direct referrer just earned
            PromoterUserModel parentOfDirectReferrer = directReferrer.getReferredBy();

            if (parentOfDirectReferrer != null) {
                BigDecimal overrideCommission = directCommission
                        .multiply(new BigDecimal("0.15"))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

                BigDecimal parentCurrentCommission = parentOfDirectReferrer.getCommissionBalance() == null
                        ? BigDecimal.ZERO
                        : parentOfDirectReferrer.getCommissionBalance();

                parentOfDirectReferrer.setCommissionBalance(
                        parentCurrentCommission.add(overrideCommission)
                );

                userRepository.save(parentOfDirectReferrer);
            }
        }

        BigDecimal currentSystemShare = paidUser.getSystemShareRecorded() == null
                ? BigDecimal.ZERO
                : paidUser.getSystemShareRecorded();

        paidUser.setSystemShareRecorded(currentSystemShare.add(systemShare));
        paidUser.setPaymentVerified(true);

        userRepository.save(paidUser);
    }

    private String createFlutterwavePayment(Map<String, Object> payload) {
        String url = flutterwaveBaseUrl + "/v3/payments";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(flutterwaveSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Flutterwave payment initialization failed");
        }

        Object statusObj = body.get("status");
        String status = statusObj == null ? null : statusObj.toString();

        if (status == null || !"success".equalsIgnoreCase(status)) {
            throw new RuntimeException("Flutterwave payment initialization failed");
        }

        Object dataObj = body.get("data");
        if (!(dataObj instanceof Map)) {
            throw new RuntimeException("Flutterwave did not return payment data");
        }

        Map data = (Map) dataObj;
        Object linkObj = data.get("link");
        String paymentLink = linkObj == null ? null : linkObj.toString();

        if (paymentLink == null || paymentLink.isBlank()) {
            throw new RuntimeException("Flutterwave did not return payment link");
        }

        return paymentLink;
    }

    private Map verifyTransactionWithFlutterwave(String flutterwaveTransactionId) {
        if (flutterwaveTransactionId == null || flutterwaveTransactionId.isBlank()) {
            throw new RuntimeException("Missing Flutterwave transaction id");
        }

        String url = flutterwaveBaseUrl + "/v3/transactions/" + flutterwaveTransactionId + "/verify";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(flutterwaveSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map body = response.getBody();

        if (body == null) {
            throw new RuntimeException("Flutterwave verification failed");
        }

        Object statusObj = body.get("status");
        String status = statusObj == null ? null : statusObj.toString();

        if (status == null || !"success".equalsIgnoreCase(status)) {
            throw new RuntimeException("Flutterwave verification failed");
        }

        return body;
    }

    private boolean isValidWebhook(String verifHash, String flutterwaveSignature, String rawBody) {
        if (verifHash != null && !verifHash.isBlank()) {
            return flutterwaveSecretHash.equals(verifHash);
        }

        if (flutterwaveSignature != null && !flutterwaveSignature.isBlank()) {
            String computed = hmacSha256Base64(rawBody, flutterwaveSecretHash);
            return computed.equals(flutterwaveSignature);
        }

        return false;
    }

    private String hmacSha256Base64(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute webhook signature", e);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) {
            return null;
        }

        int firstQuote = json.indexOf("\"", colonIndex + 1);
        if (firstQuote == -1) {
            return null;
        }

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) {
            return null;
        }

        return json.substring(firstQuote + 1, secondQuote);
    }
    /*public Map<String, Object> getRegistrationPaymentStatus(Integer userId) {
        PromoterUserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean verified = user.getPaymentVerified() != null && user.getPaymentVerified();
        Transaction tx = transactionRepo.findByUserIdAndPaymentType(userId, "REGISTRATION_PAYMENT")
                .stream()
                .findFirst()
                .orElse(null);

        BigDecimal paidAmount = tx != null && tx.getPaidAmount() != null
                ? tx.getPaidAmount()
                : BigDecimal.ZERO;

        String status = tx != null && tx.getStatus() != null
                ? tx.getStatus()
                : "UNKNOWN";

        return Map.of(
                "userId", user.getId(),
                "verified", verified,
                "paymentVerified", verified,
                "canLogin", verified,
                "message", verified ? "Activation fee verified" : "Activation fee not yet verified",
                "paidAmount", paidAmount,
                "status", status
        );
    }*/
    public Map<String, Object> getRegistrationPaymentStatus(Integer userId) {
        PromoterUserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> txs = transactionRepo.findByUserIdAndPaymentType(userId, "REGISTRATION_PAYMENT");

        Transaction tx = txs.stream()
                .filter(t -> t.getCreatedAt() != null)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElse(
                        txs.stream()
                                .filter(t -> t.getCreatedAt() != null)
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .findFirst()
                                .orElse(null)
                );

        boolean verified = user.getPaymentVerified() != null && user.getPaymentVerified();

        BigDecimal paidAmount = tx != null && tx.getPaidAmount() != null
                ? tx.getPaidAmount()
                : BigDecimal.ZERO;

        String status = tx != null && tx.getStatus() != null
                ? tx.getStatus()
                : "UNKNOWN";

        return Map.of(
                "userId", user.getId(),
                "verified", verified,
                "paymentVerified", verified,
                "canLogin", verified,
                "message", verified ? "Activation fee verified" : "Activation fee not yet verified",
                "paidAmount", paidAmount,
                "status", status
        );
    }

    public Transaction reconcilePendingPayment(String txRef) {
        Transaction tx = transactionRepo.findByTxRef(txRef.trim())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"REGISTRATION_PAYMENT".equalsIgnoreCase(tx.getPaymentType())) {
            throw new RuntimeException("Only registration payments can be reconciled here");
        }

        if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
            return tx;
        }

        return verifyAndProcessPaymentByReference(txRef);
    }
    public Transaction verifyAndProcessPaymentByReference(String txRef) {
        String cleanTxRef = txRef == null ? "" : txRef.trim();

        String url = flutterwaveBaseUrl + "/v3/transactions/verify_by_reference?tx_ref=" + cleanTxRef;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(flutterwaveSecretKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !"success".equalsIgnoreCase(String.valueOf(body.get("status")))) {
            throw new RuntimeException("Unable to verify payment with Flutterwave");
        }

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        if (data == null) {
            throw new RuntimeException("No payment data returned from Flutterwave");
        }

        String returnedTxRef = String.valueOf(data.get("tx_ref"));
        String status = String.valueOf(data.get("status"));
        String currency = String.valueOf(data.get("currency"));
        Object amountObj = data.get("amount");
        Object idObj = data.get("id");

        if (!cleanTxRef.equals(returnedTxRef)) {
            throw new RuntimeException("Verified tx_ref does not match local transaction");
        }

        if (!"successful".equalsIgnoreCase(status)) {
            throw new RuntimeException("Payment is not successful on Flutterwave");
        }

        Transaction tx = transactionRepo.findByTxRef(cleanTxRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        BigDecimal verifiedAmount = new BigDecimal(String.valueOf(amountObj));

        if (!"NGN".equalsIgnoreCase(currency)) {
            throw new RuntimeException("Invalid currency");
        }

        if (verifiedAmount.compareTo(tx.getExpectedAmount()) < 0) {
            throw new RuntimeException("Paid amount is less than expected amount");
        }

        tx.setStatus("SUCCESS");
        tx.setPaidAmount(tx.getExpectedAmount());
        tx.setFlutterwaveTransactionId(idObj == null ? null : String.valueOf(idObj));
        tx.setVerifiedAt(LocalDateTime.now());

        transactionRepo.save(tx);

        applyBusinessSuccess(tx);

        return tx;
    }

    public List<Map<String, Object>> getPendingRegistrationPayments() {
        return transactionRepo.findAll().stream()
                .filter(t -> "REGISTRATION_PAYMENT".equalsIgnoreCase(t.getPaymentType()))
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(t -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("txRef", t.getTxRef());
                    item.put("userId", t.getUser().getId());
                    item.put("name", t.getUser().getName() == null ? "" : t.getUser().getName());
                    item.put("email", t.getUser().getEmail() == null ? "" : t.getUser().getEmail());
                    item.put("amount", t.getExpectedAmount() == null ? BigDecimal.ZERO : t.getExpectedAmount());
                    item.put("status", t.getStatus());
                    item.put("createdAt", t.getCreatedAt() == null ? "" : t.getCreatedAt().toString());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    public List<Map<String, Object>> getAllRegistrationPayments() {
        return transactionRepo.findAll().stream()
                .filter(t -> "REGISTRATION_PAYMENT".equalsIgnoreCase(t.getPaymentType()))
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(t -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", t.getId());
                    item.put("txRef", t.getTxRef());
                    item.put("userId", t.getUser() != null ? t.getUser().getId() : null);
                    item.put("name", t.getUser() != null && t.getUser().getName() != null ? t.getUser().getName() : "");
                    item.put("email", t.getUser() != null && t.getUser().getEmail() != null ? t.getUser().getEmail() : "");
                    item.put("amount", t.getExpectedAmount() != null ? t.getExpectedAmount() : BigDecimal.ZERO);
                    item.put("paidAmount", t.getPaidAmount() != null ? t.getPaidAmount() : BigDecimal.ZERO);
                    item.put("status", t.getStatus() != null ? t.getStatus() : "UNKNOWN");
                    item.put("paymentType", t.getPaymentType());
                    item.put("flutterwaveTransactionId", t.getFlutterwaveTransactionId());
                    item.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
                    item.put("verifiedAt", t.getVerifiedAt() != null ? t.getVerifiedAt().toString() : "");
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}