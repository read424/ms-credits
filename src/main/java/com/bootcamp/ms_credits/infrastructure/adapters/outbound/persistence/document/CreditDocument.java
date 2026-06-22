package com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.document;

import com.bootcamp.ms_credits.domain.model.CreditStatus;
import com.bootcamp.ms_credits.domain.model.CreditType;
import com.bootcamp.ms_credits.domain.model.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credits")
public class CreditDocument {

    @Id
    private String id;

    @Field("credit_number")
    private String creditNumber;

    private CreditType type;

    @Field("customer_id")
    private String customerId;

    @Field("customer_type")
    private CustomerType customerType;

    private Double amount;

    @Field("outstanding_balance")
    private Double outstandingBalance;

    @Field("interest_rate")
    private Double interestRate;

    @Field("credit_limit")
    private Double creditLimit;

    @Field("available_credit")
    private Double availableCredit;

    private CreditStatus status;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;
}
