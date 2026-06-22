package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto;

import com.bootcamp.ms_credits.domain.model.CreditStatus;
import com.bootcamp.ms_credits.domain.model.CreditType;
import com.bootcamp.ms_credits.domain.model.CustomerType;
import lombok.Data;

@Data
public class CreditResponse {
    private String id;
    private String creditNumber;
    private CreditType type;
    private String customerId;
    private CustomerType customerType;
    private Double amount;
    private Double outstandingBalance;
    private Double interestRate;
    private Double creditLimit;
    private Double availableCredit;
    private CreditStatus status;
}
