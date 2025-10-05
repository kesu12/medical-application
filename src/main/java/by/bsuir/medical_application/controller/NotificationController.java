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
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForDoctor(@PathVariable Long doctorId) {
        List<NotificationDto> notifications = notificationService.getNotificationsForDoctor(doctorId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/nurse/{nurseId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForNurse(@PathVariable Long nurseId) {
        List<NotificationDto> notifications = notificationService.getNotificationsForNurse(nurseId);
        return ResponseEntity.ok(notifications);
    }
}
