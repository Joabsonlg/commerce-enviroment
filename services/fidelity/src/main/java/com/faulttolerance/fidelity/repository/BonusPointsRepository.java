package com.faulttolerance.fidelity.repository;

import com.faulttolerance.fidelity.model.BonusPoints;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BonusPointsRepository extends MongoRepository<BonusPoints, String> {
    BonusPoints findByUserIdAndOrderId(Long userId, Long orderId);
    long countByStatus(String status);
}
