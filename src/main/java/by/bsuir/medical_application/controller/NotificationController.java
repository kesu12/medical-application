package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.NotificationDto;
import by.bsuir.medical_application.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getNotificationsForDoctor(doctorId));
    }
    
    @GetMapping("/nurse/{nurseId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForNurse(@PathVariable Long nurseId) {
        return ResponseEntity.ok(notificationService.getNotificationsForNurse(nurseId));
    }
    
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId,
                                                       @RequestParam Long userId) {
        notificationService.markNotificationAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }
}
