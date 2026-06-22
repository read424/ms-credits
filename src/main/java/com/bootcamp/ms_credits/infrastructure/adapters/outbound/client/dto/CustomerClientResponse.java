package com.bootcamp.ms_credits.infrastructure.adapters.outbound.client.dto;

import com.bootcamp.ms_credits.domain.model.CustomerType;
import lombok.Data;

@Data
public class CustomerClientResponse {
    private String id;
    private String name;
    private CustomerType customerType;
}
