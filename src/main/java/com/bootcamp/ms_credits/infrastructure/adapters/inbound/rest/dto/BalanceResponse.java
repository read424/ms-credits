package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto;

import com.bootcamp.ms_credits.domain.model.CreditType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponse {
    private String creditId;
    private CreditType type;
    private Double outstandingBalance;
    private Double creditLimit;
    private Double availableCredit;
}
