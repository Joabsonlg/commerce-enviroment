package com.faulttolerance.exchange.repository;

import com.faulttolerance.exchange.model.ExchangeRate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {
}
