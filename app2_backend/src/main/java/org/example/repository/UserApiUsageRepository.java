package org.example.repository;

import org.example.entity.UserApiUsage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserApiUsageRepository extends MongoRepository<UserApiUsage, String> {
}
