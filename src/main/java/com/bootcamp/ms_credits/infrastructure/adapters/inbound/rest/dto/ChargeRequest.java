package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ChargeRequest {

    @Positive
    private Double amount;

    private String description;
}
