package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByOwner(User owner);

    Page<Card> findByOwner(User owner, Pageable pageable);

    Page<Card> findByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.owner.id = :userId")
    List<Card> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Card c WHERE c.owner.id = :userId AND c.status = :status")
    List<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status);

    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.owner.id = :userId AND c.status = 'ACTIVE'")
    long countActiveCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Card c WHERE c.id = :cardId AND c.owner.id = :userId")
    Optional<Card> findByIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);
}