package com.bootcamp.ms_credits.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCreatedEvent {
    private String id;
    private String creditNumber;
    private String customerId;
    private String type;
    private Double amount;
    private Double outstandingBalance;
    private Double interestRate;
    private Double creditLimit;
    private Double availableCredit;
    private String customerType;
    private String status;
    private LocalDateTime createdAt;
}
