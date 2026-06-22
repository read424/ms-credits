package com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.mapper;

import com.bootcamp.ms_credits.domain.model.Credit;
import com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.document.CreditDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditDocumentMapper {
    CreditDocument toDocument(Credit credit);
    Credit toDomain(CreditDocument document);
}
