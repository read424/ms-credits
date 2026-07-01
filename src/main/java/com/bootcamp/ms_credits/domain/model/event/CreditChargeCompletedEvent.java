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
public class CreditChargeCompletedEvent {
    private String id;
    private String creditNumber;
    private String customerId;
    private String type;
    private Double amount;
    private Double newAvailableCredit;
    private String description;
    private String transactionId;
    private LocalDateTime completedAt;
}
