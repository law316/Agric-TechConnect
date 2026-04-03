package net.ikwa.techconnect.userregDTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawalRequest {

    private BigDecimal amount;
    private String bankCode;
    private String accountNumber;
    private String accountName;
}