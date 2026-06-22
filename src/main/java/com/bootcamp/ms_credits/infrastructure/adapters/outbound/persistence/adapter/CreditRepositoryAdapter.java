package com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.adapter;

import com.bootcamp.ms_credits.application.ports.out.CreditRepositoryPort;
import com.bootcamp.ms_credits.domain.model.Credit;
import com.bootcamp.ms_credits.domain.model.CreditType;
import com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.mapper.CreditDocumentMapper;
import com.bootcamp.ms_credits.infrastructure.adapters.outbound.persistence.repository.CreditMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CreditRepositoryAdapter implements CreditRepositoryPort {

    private final CreditMongoRepository mongoRepository;
    private final CreditDocumentMapper mapper;

    @Override
    public Mono<Credit> save(Credit credit) {
        return mongoRepository.save(mapper.toDocument(credit))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Credit> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Credit> findAll() {
        return mongoRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Credit> findByCustomerId(String customerId) {
        return mongoRepository.findByCustomerId(customerId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByCustomerIdAndType(String customerId, CreditType type) {
        return mongoRepository.countByCustomerIdAndType(customerId, type);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
