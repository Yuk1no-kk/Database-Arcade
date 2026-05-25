package com.example.arcadesystem.model;

import java.math.BigDecimal;

public class Member {
    private Integer memberId;
    private String name;
    private String phone;
    private Integer tokenBalance;
    private String vipLevel;
    private BigDecimal accumulatedSpend;

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getTokenBalance() { return tokenBalance; }
    public void setTokenBalance(Integer tokenBalance) { this.tokenBalance = tokenBalance; }

    public String getVipLevel() { return vipLevel; }
    public void setVipLevel(String vipLevel) { this.vipLevel = vipLevel; }

    public BigDecimal getAccumulatedSpend() { return accumulatedSpend; }
    public void setAccumulatedSpend(BigDecimal accumulatedSpend) { this.accumulatedSpend = accumulatedSpend; }
}
