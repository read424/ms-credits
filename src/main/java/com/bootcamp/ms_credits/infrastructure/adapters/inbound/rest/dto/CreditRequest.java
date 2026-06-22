package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto;

import com.bootcamp.ms_credits.domain.model.CreditType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreditRequest {

    @NotBlank
    private String customerId;

    @NotNull
    private CreditType type;

    @PositiveOrZero
    private Double amount;

    @PositiveOrZero
    private Double interestRate;

    @PositiveOrZero
    private Double creditLimit;
}
