package org.example.repository;

import org.example.entity.TarotCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarotCardRepository extends MongoRepository<TarotCard, String> {
    List<TarotCard> findByCategory(String category);
    List<TarotCard> findByElementalAffinity(String elementalAffinity);
}