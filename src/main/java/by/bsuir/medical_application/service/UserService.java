package by.bsuir.medical_application.service;

import by.bsuir.medical_application.dto.BulkUserUpdateDto;
import by.bsuir.medical_application.dto.UserAdminUpdateDto;
import by.bsuir.medical_application.dto.UserCreateDto;
import by.bsuir.medical_application.dto.UserUpdateDto;
import by.bsuir.medical_application.exceptions.AccountCreatingException;
import by.bsuir.medical_application.exceptions.AccountUpdatingException;
import by.bsuir.medical_application.model.Department;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.repository.DepartmentRepository;
import by.bsuir.medical_application.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

//    public User createUser(UserCreateDto userDto) {
//
//        if (userRepository.existsByUsername(userDto.getUsername())) {
//            throw new AccountCreatingException("Username already exists");
//        }
//        if (userRepository.existsByEmail(userDto.getEmail())) {
//            throw new AccountCreatingException("Email already exists");
//        }
//
//        if(userDto.getPassword().length() < 6) {
//            throw new AccountCreatingException("Password too short");
//        }
//
//        if(!userDto.getPassword().equals(userDto.getConfirmPassword())) {
//            throw new AccountCreatingException("Passwords do not match");
//        }
//
//        User user = new User();
//        user.setUsername(userDto.getUsername());
//        user.setEmail(userDto.getEmail());
//        user.setPassword(userDto.getPassword());
//        user.setFirstName(userDto.getFirstName());
//        user.setMiddleName(userDto.getMiddleName());
//        user.setLastName(userDto.getLastName());
//
//
//
//        return userRepository.save(user);
//    }

    public List<User> getUnconfirmedUsers() {
        return userRepository.findAllByRole(UserRole.DEFAULT);
    }

    public User confirmUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setConfirmed(true);
            
            if (user.getRole() == UserRole.DEFAULT) {
                user.setRole(UserRole.PATIENT);
                log.info("User {} confirmed and assigned PATIENT role", user.getUsername());
            }
            
            return userRepository.save(user);
        }
        throw new AccountUpdatingException("User not found with id: " + userId);
    }

    public User giveRole(Long userId, UserRole role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(role);
            log.info("User {} role changed to {}", user.getUsername(), role);
            return userRepository.save(user);
        }
        throw new AccountUpdatingException("User not found with id: " + userId);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.getUserByUserId(id);
    }

    public User updateUserPassword(Long userId, UserUpdateDto userUpdateDto) {
        User user = getUserById(userId);

        if (!user.getPassword().equals(userUpdateDto.getPassword())){
            throw new AccountUpdatingException("You entered wrong password");
        }

        if(!userUpdateDto.getNewPassword().equals(userUpdateDto.getConfirmPassword())){
            throw new AccountUpdatingException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(userUpdateDto.getNewPassword()));
        return user;
    }

    public User deactivateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> u.setConfirmed(false));
        return user.orElse(null);
    }

    public User activateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> u.setConfirmed(true));
        return user.orElse(null);
    }

    public void deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("User with id {} deleted", userId);
        } else {
            throw new AccountUpdatingException("User not found with id: " + userId);
        }
    }

    public User updateUserAdmin(Long userId, UserAdminUpdateDto adminUpdateDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (adminUpdateDto.getRole() != null) {
                user.setRole(adminUpdateDto.getRole());
            }
            
            user.setConfirmed(adminUpdateDto.isConfirmed());
            
            if (adminUpdateDto.isConfirmed() && adminUpdateDto.getDepartmentId() != null) {
                Optional<Department> departmentOpt = departmentRepository.findById(adminUpdateDto.getDepartmentId());
                if (departmentOpt.isPresent()) {
                    user.setDepartment(departmentOpt.get());
                    log.info("User {} assigned to department {}", user.getUsername(), departmentOpt.get().getName());
                }
            }
            
            log.info("User {} updated by admin", user.getUsername());
            return userRepository.save(user);
        }
        throw new AccountUpdatingException("User not found with id: " + userId);
    }

    public User confirmUserWithDepartment(Long userId, Long departmentId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        
        if (userOpt.isPresent() && departmentOpt.isPresent()) {
            User user = userOpt.get();
            Department department = departmentOpt.get();
            
            user.setConfirmed(true);
            user.setRole(UserRole.NURSE);
            user.setDepartment(department);
            
            log.info("User {} confirmed, assigned NURSE role and department {}", 
                    user.getUsername(), department.getName());
            
            return userRepository.save(user);
        }
        throw new AccountUpdatingException("User or department not found");
    }

    public List<User> getConfirmedUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getConfirmed() != null && user.getConfirmed())
                .toList();
    }

    public List<User> getUnconfirmedUsersDetailed() {
        return userRepository.findAll().stream()
                .filter(user -> user.getConfirmed() == null || !user.getConfirmed())
                .toList();
    }


    public List<User> bulkUpdateUsers(BulkUserUpdateDto bulkUpdateDto) {
        List<User> updatedUsers = bulkUpdateDto.getUserIds().stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (updatedUsers.isEmpty()) {
            throw new AccountUpdatingException("No valid users found for bulk update");
        }

        switch (bulkUpdateDto.getAction()) {
            case CONFIRM -> {
                updatedUsers.forEach(user -> {
                    user.setConfirmed(true);
                    if (user.getRole() == UserRole.DEFAULT) {
                        user.setRole(UserRole.NURSE);
                    }
                });
                log.info("Bulk confirmed {} users", updatedUsers.size());
            }
            case DEACTIVATE -> {
                updatedUsers.forEach(user -> user.setConfirmed(false));
                log.info("Bulk deactivated {} users", updatedUsers.size());
            }
            case CHANGE_ROLE -> {
                if (bulkUpdateDto.getRole() == null) {
                    throw new AccountUpdatingException("Role is required for role change");
                }
                updatedUsers.forEach(user -> user.setRole(bulkUpdateDto.getRole()));
                log.info("Bulk changed role to {} for {} users", bulkUpdateDto.getRole(), updatedUsers.size());
            }
            case ASSIGN_DEPARTMENT -> {
                if (bulkUpdateDto.getDepartmentId() == null) {
                    throw new AccountUpdatingException("Department ID is required for department assignment");
                }
                Optional<Department> departmentOpt = departmentRepository.findById(bulkUpdateDto.getDepartmentId());
                if (departmentOpt.isEmpty()) {
                    throw new AccountUpdatingException("Department not found");
                }
                Department department = departmentOpt.get();
                updatedUsers.forEach(user -> user.setDepartment(department));
                log.info("Bulk assigned department {} to {} users", department.getName(), updatedUsers.size());
            }
            case DELETE -> {
                userRepository.deleteAll(updatedUsers);
                log.info("Bulk deleted {} users", updatedUsers.size());
                return List.of();
            }
        }

        return userRepository.saveAll(updatedUsers);
    }

    public int bulkConfirmUsers(List<Long> userIds) {
        List<User> users = userIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        users.forEach(user -> {
            user.setConfirmed(true);
            if (user.getRole() == UserRole.DEFAULT) {
                user.setRole(UserRole.PATIENT);
            }
        });

        userRepository.saveAll(users);
        log.info("Bulk confirmed {} users", users.size());
        return users.size();
    }


    public User assignNurseToPatient(Long patientId, Long nurseId) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        Optional<User> nurseOpt = userRepository.findById(nurseId);
        
        if (patientOpt.isEmpty() || nurseOpt.isEmpty()) {
            throw new AccountUpdatingException("Patient or Nurse not found");
        }
        
        User patient = patientOpt.get();
        User nurse = nurseOpt.get();
        
        if (patient.getRole() != UserRole.PATIENT) {
            throw new AccountUpdatingException("User is not a patient");
        }
        
        if (nurse.getRole() != UserRole.NURSE) {
            throw new AccountUpdatingException("User is not a nurse");
        }
        
        patient.setAssignedNurse(nurse);
        log.info("Nurse {} assigned to patient {}", nurse.getUsername(), patient.getUsername());
        
        return userRepository.save(patient);
    }

    public User assignDoctorToPatient(Long patientId, Long doctorId) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        Optional<User> doctorOpt = userRepository.findById(doctorId);
        
        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            throw new AccountUpdatingException("Patient or Doctor not found");
        }
        
        User patient = patientOpt.get();
        User doctor = doctorOpt.get();
        
        if (patient.getRole() != UserRole.PATIENT) {
            throw new AccountUpdatingException("User is not a patient");
        }
        
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new AccountUpdatingException("User is not a doctor");
        }
        
        patient.setAssignedDoctor(doctor);
        log.info("Doctor {} assigned to patient {}", doctor.getUsername(), patient.getUsername());
        
        return userRepository.save(patient);
    }

    public User assignDepartmentToPatient(Long patientId, Long departmentId) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        
        if (patientOpt.isEmpty() || departmentOpt.isEmpty()) {
            throw new AccountUpdatingException("Patient or Department not found");
        }
        
        User patient = patientOpt.get();
        Department department = departmentOpt.get();
        
        if (patient.getRole() != UserRole.PATIENT) {
            throw new AccountUpdatingException("User is not a patient");
        }
        
        patient.setDepartment(department);
        log.info("Department {} assigned to patient {}", department.getName(), patient.getUsername());
        
        return userRepository.save(patient);
    }

    public List<User> getActivePatients() {
        return userRepository.findByRoleAndConfirmed(UserRole.PATIENT, true);
    }

    public List<User> getPatientsByNurse(Long nurseId) {
        return userRepository.findByAssignedNurseUserId(nurseId);
    }

    public List<User> getPatientsByDoctor(Long doctorId) {
        return userRepository.findByAssignedDoctorUserId(doctorId);
    }

    public List<User> getPatientsWithoutDoctor() {
        return userRepository.findByRoleAndAssignedDoctorIsNull(UserRole.PATIENT);
    }

    public List<User> getPatientsByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, UserRole.PATIENT);
    }

    public List<User> getNursesByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, UserRole.NURSE);
    }

    public List<User> getDoctorsByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndRole(departmentId, UserRole.DOCTOR);
    }

    public User assignDepartmentToDoctor(Long doctorId, Long departmentId) {
        Optional<User> doctorOpt = userRepository.findById(doctorId);
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        
        if (doctorOpt.isEmpty() || departmentOpt.isEmpty()) {
            throw new AccountUpdatingException("Doctor or Department not found");
        }
        
        User doctor = doctorOpt.get();
        Department department = departmentOpt.get();
        
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new AccountUpdatingException("User is not a doctor");
        }
        
        doctor.setDepartment(department);
        log.info("Department {} assigned to doctor {}", department.getName(), doctor.getUsername());
        
        return userRepository.save(doctor);
    }
}
