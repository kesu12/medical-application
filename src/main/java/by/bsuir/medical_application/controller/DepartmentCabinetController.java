package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.NotificationDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.service.NotificationService;
import by.bsuir.medical_application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/department-cabinet")
public class DepartmentCabinetController {
    
    private final UserService userService;
    private final NotificationService notificationService;
    
    public DepartmentCabinetController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }
    
    @GetMapping("/{departmentId}/patients")
    public ResponseEntity<List<UserResponseDto>> getPatientsInDepartment(@PathVariable Long departmentId) {
        List<User> patients = userService.getPatientsByDepartment(departmentId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{departmentId}/nurses")
    public ResponseEntity<List<UserResponseDto>> getNursesInDepartment(@PathVariable Long departmentId) {
        List<User> nurses = userService.getNursesByDepartment(departmentId);
        List<UserResponseDto> response = nurses.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{departmentId}/doctors")
    public ResponseEntity<List<UserResponseDto>> getDoctorsInDepartment(@PathVariable Long departmentId) {
        List<User> doctors = userService.getDoctorsByDepartment(departmentId);
        List<UserResponseDto> response = doctors.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{departmentId}/notifications")
    public ResponseEntity<List<NotificationDto>> getDepartmentNotifications(@PathVariable Long departmentId) {
        List<User> doctors = userService.getDoctorsByDepartment(departmentId);
        List<NotificationDto> allNotifications = new ArrayList<>();
        
        for (User doctor : doctors) {
            List<NotificationDto> doctorNotifications = notificationService.getNotificationsForDoctor(doctor.getUserId());
            allNotifications.addAll(doctorNotifications);
        }
        
        return ResponseEntity.ok(allNotifications);
    }
    
}
