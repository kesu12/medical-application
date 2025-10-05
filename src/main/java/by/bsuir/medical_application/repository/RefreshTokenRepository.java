package by.bsuir.medical_application.repository;

import by.bsuir.medical_application.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
        Optional<RefreshToken> findByToken(String token);

        void deleteByToken(String token);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("DELETE FROM RefreshToken rt WHERE rt.user.userId = :userId")
        void deleteByUserId(@Param("userId") Long userId);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
        void deleteAllExpiredSince(@Param("now") Instant now);
}
