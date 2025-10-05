package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.PatientAssignmentDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nurse-cabinet")
public class NurseCabinetController {
    
    private final UserService userService;
    
    public NurseCabinetController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{nurseId}/patients")
    public ResponseEntity<List<UserResponseDto>> getAssignedPatients(@PathVariable Long nurseId) {
        List<User> patients = userService.getPatientsByNurse(nurseId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{nurseId}/notifications")
    public ResponseEntity<List<String>> getNurseNotifications(@PathVariable Long nurseId) {
        return ResponseEntity.ok(List.of("No notifications available"));
    }
    
    @PostMapping("/assign-department-to-patient")
    public ResponseEntity<UserResponseDto> assignDepartmentToPatient(@RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignDepartmentToPatient(assignmentDto.getPatientId(), assignmentDto.getDepartmentId());
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/assign-nurse-to-patient")
    public ResponseEntity<UserResponseDto> assignNurseToPatient(@RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignNurseToPatient(assignmentDto.getPatientId(), assignmentDto.getNurseId());
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
}

