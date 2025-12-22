package main.java.com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.TransferStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByUser(User user);

    Page<Transfer> findByUser(User user, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.user.id = :userId")
    Page<Transfer> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    List<Transfer> findByCardId(@Param("cardId") Long cardId);

    @Query("SELECT t FROM Transfer t WHERE " +
           "(t.fromCard.id = :cardId OR t.toCard.id = :cardId) " +
           "AND t.user.id = :userId")
    Page<Transfer> findByCardIdAndUserId(@Param("cardId") Long cardId, 
                                         @Param("userId") Long userId, 
                                         Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.user.id = :userId AND t.status = :status")
    List<Transfer> findByUserIdAndStatus(@Param("userId") Long userId, 
                                         @Param("status") TransferStatus status);

    @Query("SELECT t FROM Transfer t WHERE t.transferDate BETWEEN :startDate AND :endDate")
    List<Transfer> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transfer t WHERE t.user.id = :userId " +
           "AND t.transferDate BETWEEN :startDate AND :endDate")
    List<Transfer> findByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}