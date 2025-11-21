package by.bsuir.medical_application.repository;

import by.bsuir.medical_application.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByRecipientUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByRecipientUserIdAndReadFalse(Long userId);
    long countByRecipientUserIdAndReadFalse(Long userId);
}


