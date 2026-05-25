package com.example.arcadesystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TokenTransaction {
    private Integer transactionId;
    private Integer memberId;
    private Integer packageId;
    private LocalDateTime transactionDate;
    private BigDecimal amountPaid;
    private Integer tokensPurchased;

    // join fields
    private String memberName;
    private String packageName;
    private String machineName;

    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public Integer getTokensPurchased() { return tokensPurchased; }
    public void setTokensPurchased(Integer tokensPurchased) { this.tokensPurchased = tokensPurchased; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
}
