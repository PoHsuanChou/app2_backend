package org.example.repository;

import org.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);  // This returns Optional<User>
    List<User> findByIsOnline(boolean isOnline);
    Optional<User> findByEmail(String email);  // This returns Optional<User>
}

