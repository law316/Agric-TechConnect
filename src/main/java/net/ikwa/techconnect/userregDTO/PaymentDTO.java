package net.ikwa.techconnect.userregDTO;

public class PaymentDTO {

    private String txRef;
    private String paymentLink;
    private String status;

    public PaymentDTO() {
    }

    public PaymentDTO(String txRef, String paymentLink, String status) {
        this.txRef = txRef;
        this.paymentLink = paymentLink;
        this.status = status;
    }

    public String getTxRef() {
        return txRef;
    }

    public void setTxRef(String txRef) {
        this.txRef = txRef;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}