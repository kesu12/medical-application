package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.NotificationDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.service.NotificationService;
import by.bsuir.medical_application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor-cabinet")
public class DoctorCabinetController {
    
    private final UserService userService;
    private final NotificationService notificationService;
    
    public DoctorCabinetController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }
    
    @GetMapping("/{doctorId}/patients")
    public ResponseEntity<List<UserResponseDto>> getAssignedPatients(@PathVariable Long doctorId) {
        List<User> patients = userService.getPatientsByDoctor(doctorId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{doctorId}/notifications")
    public ResponseEntity<List<NotificationDto>> getDoctorNotifications(@PathVariable Long doctorId) {
        List<NotificationDto> notifications = notificationService.getNotificationsForDoctor(doctorId);
        return ResponseEntity.ok(notifications);
    }
    
}

