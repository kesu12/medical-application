package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.PatientAssignmentDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient-cabinet")
public class PatientCabinetController {
    
    private final UserService userService;
    
    public PatientCabinetController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{patientId}/info")
    public ResponseEntity<UserResponseDto> getPatientInfo(@PathVariable Long patientId) {
        User patient = userService.getUserById(patientId);
        return ResponseEntity.ok(new UserResponseDto(patient));
    }
    
    @PostMapping("/assign-nurse")
    public ResponseEntity<UserResponseDto> assignNurse(@RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignNurseToPatient(assignmentDto.getPatientId(), assignmentDto.getNurseId());
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/available-nurses")
    public ResponseEntity<List<UserResponseDto>> getAvailableNurses() {
        List<User> nurses = userService.getUsersByRole(UserRole.NURSE);
        List<UserResponseDto> response = nurses.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available-doctors")
    public ResponseEntity<List<UserResponseDto>> getAvailableDoctors() {
        List<User> doctors = userService.getUsersByRole(UserRole.DOCTOR);
        List<UserResponseDto> response = doctors.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
