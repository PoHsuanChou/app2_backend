package org.example.repository;

import org.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);  // This returns Optional<User>
    List<User> findByIsOnline(boolean isOnline);
    Optional<User> findByEmail(String email);  // This returns Optional<User>

    List<User> findByIdIn(List<String> ids);

    @Query("{ 'profile.gender': ?0 }")
    List<User> findByGender(String gender);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'seenUserIds': 1 }")
    Optional<User> findSeenUserIdsByUserId(String userId);

    @Query("{ '_id': { $nin: ?0 }, 'id': { $ne: ?1 } }")
    List<User> findRandomUsersExcluding(List<String> seenUserIds, String userId);

    List<User> findAllByIdNot(String id); // 查找 ID 不等於指定值的用戶
}

