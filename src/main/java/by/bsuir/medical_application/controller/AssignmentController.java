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
@RequestMapping("/api/assignments")
public class AssignmentController {
    
    private final UserService userService;
    
    public AssignmentController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/patients/{patientId}/nurse")
    public ResponseEntity<UserResponseDto> assignNurseToPatient(@PathVariable Long patientId, @RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignNurseToPatient(patientId, assignmentDto.getNurseId());
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/patients/{patientId}/doctor")
    public ResponseEntity<UserResponseDto> assignDoctorToPatient(@PathVariable Long patientId, @RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignDoctorToPatient(patientId, assignmentDto.getDoctorId());
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/patients/{patientId}/department")
    public ResponseEntity<UserResponseDto> assignDepartmentToPatient(@PathVariable Long patientId, @RequestBody PatientAssignmentDto assignmentDto) {
        try {
            User patient = userService.assignDepartmentToPatient(patientId, assignmentDto.getDepartmentId());
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new UserResponseDto(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/active-patients")
    public ResponseEntity<List<UserResponseDto>> getActivePatients() {
        try {
            List<User> patients = userService.getActivePatients();
            List<UserResponseDto> response = patients.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/patients-by-nurse/{nurseId}")
    public ResponseEntity<List<UserResponseDto>> getPatientsByNurse(@PathVariable Long nurseId) {
        List<User> patients = userService.getPatientsByNurse(nurseId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patients-by-doctor/{doctorId}")
    public ResponseEntity<List<UserResponseDto>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<User> patients = userService.getPatientsByDoctor(doctorId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patients-without-doctor")
    public ResponseEntity<List<UserResponseDto>> getPatientsWithoutDoctor() {
        List<User> patients = userService.getPatientsWithoutDoctor();
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patients-by-department/{departmentId}")
    public ResponseEntity<List<UserResponseDto>> getPatientsByDepartment(@PathVariable Long departmentId) {
        List<User> patients = userService.getPatientsByDepartment(departmentId);
        List<UserResponseDto> response = patients.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/nurses-by-department/{departmentId}")
    public ResponseEntity<List<UserResponseDto>> getNursesByDepartment(@PathVariable Long departmentId) {
        List<User> nurses = userService.getNursesByDepartment(departmentId);
        List<UserResponseDto> response = nurses.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/doctors-by-department/{departmentId}")
    public ResponseEntity<List<UserResponseDto>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<User> doctors = userService.getDoctorsByDepartment(departmentId);
        List<UserResponseDto> response = doctors.stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all-nurses")
    public ResponseEntity<List<UserResponseDto>> getAllNurses() {
        try {
            List<User> nurses = userService.getUsersByRole(UserRole.NURSE);
            List<UserResponseDto> response = nurses.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/all-doctors")
    public ResponseEntity<List<UserResponseDto>> getAllDoctors() {
        try {
            List<User> doctors = userService.getUsersByRole(UserRole.DOCTOR);
            List<UserResponseDto> response = doctors.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponseDto> response = users.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
