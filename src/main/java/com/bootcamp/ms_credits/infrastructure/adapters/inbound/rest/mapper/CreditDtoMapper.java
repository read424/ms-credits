package com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.mapper;

import com.bootcamp.ms_credits.domain.model.Credit;
import com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto.CreditRequest;
import com.bootcamp.ms_credits.infrastructure.adapters.inbound.rest.dto.CreditResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditDtoMapper {
    Credit toDomain(CreditRequest request);
    CreditResponse toResponse(Credit credit);
}
