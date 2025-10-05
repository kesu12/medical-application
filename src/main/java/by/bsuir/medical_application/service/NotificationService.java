package by.bsuir.medical_application.service;

import by.bsuir.medical_application.dto.NotificationDto;
import by.bsuir.medical_application.model.Department;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final UserRepository userRepository;
    
    public List<NotificationDto> getNotificationsForDoctor(Long doctorId) {
        Optional<User> doctorOpt = userRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User doctor = doctorOpt.get();
        if (doctor.getDepartment() == null) {
            return new ArrayList<>();
        }
        
        Department department = doctor.getDepartment();
        List<User> patientsWithoutDoctor = userRepository.findByDepartmentIdAndRoleAndAssignedDoctorIsNull(
                department.getId(), UserRole.PATIENT);
        
        List<NotificationDto> notifications = new ArrayList<>();
        for (User patient : patientsWithoutDoctor) {
            NotificationDto notification = NotificationDto.builder()
                    .id(patient.getUserId())
                    .message("Новый пациент " + patient.getFirstName() + " " + patient.getLastName() + 
                            " в отделе " + department.getName() + " без назначенного врача")
                    .type("PATIENT_WITHOUT_DOCTOR")
                    .patientId(patient.getUserId())
                    .patientName(patient.getFirstName() + " " + patient.getLastName())
                    .departmentId(department.getId())
                    .departmentName(department.getName())
                    .createdAt(patient.getCreatedAt())
                    .read(false)
                    .build();
            notifications.add(notification);
        }
        
        return notifications;
    }
    
    public List<NotificationDto> getNotificationsForNurse(Long nurseId) {
        Optional<User> nurseOpt = userRepository.findById(nurseId);
        if (nurseOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<User> assignedPatients = userRepository.findByAssignedNurseUserId(nurseId);
        
        List<NotificationDto> notifications = new ArrayList<>();
        for (User patient : assignedPatients) {
            if (patient.getAssignedDoctor() == null) {
                NotificationDto notification = NotificationDto.builder()
                        .id(patient.getUserId())
                        .message("Пациент " + patient.getFirstName() + " " + patient.getLastName() + 
                                " без назначенного врача")
                        .type("PATIENT_WITHOUT_DOCTOR")
                        .patientId(patient.getUserId())
                        .patientName(patient.getFirstName() + " " + patient.getLastName())
                        .departmentId(patient.getDepartment() != null ? patient.getDepartment().getId() : null)
                        .departmentName(patient.getDepartment() != null ? patient.getDepartment().getName() : "Не назначен")
                        .createdAt(patient.getCreatedAt())
                        .read(false)
                        .build();
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
}
