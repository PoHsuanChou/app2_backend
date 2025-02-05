package org.example.repository;

import org.example.entity.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// Location Repository
@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
}
