package com.bootcamp.ms_credits.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pure domain entity — no framework annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
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
