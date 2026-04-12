package com.bank_notification.dto;
import java.math.BigDecimal;

public class TransactionRequestDto {
    private BigDecimal amount;
    private String type;
    private String description;
    private String receiverEmail; // New field for transfers

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }
}