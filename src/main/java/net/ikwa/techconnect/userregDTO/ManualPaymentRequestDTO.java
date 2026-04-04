package net.ikwa.techconnect.userregDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualPaymentRequestDTO {

    private Integer userId;
    private String fullName;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String phoneNumber;
    private String email;
    private BigDecimal amount;
}