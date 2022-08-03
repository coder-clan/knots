package org.coderclan.knots.demo;

import java.io.Serializable;

public class IncreaseRewardPointRequestDto implements Serializable {

    private String customerId;
    private Integer amount;


    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
