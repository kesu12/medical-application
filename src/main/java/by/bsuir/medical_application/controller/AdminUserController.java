package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.*;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/unconfirmed")
    public List<UserResponseDto> getUnconfirmedUsers() {
        return userService.getUnconfirmedUsersDetailed().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable long userId) {
        User user = userService.getUserById(userId);
        return new UserResponseDto(user);
    }

    @PatchMapping("/{userId}/confirm")
    public UserResponseDto confirmUser(@PathVariable Long userId) {
        User user = userService.confirmUser(userId);
        return new UserResponseDto(user);
    }

    @GetMapping("/by-role/{role}")
    public List<UserResponseDto> getUsersByRole(@PathVariable UserRole role) {
        return userService.getUsersByRole(role).stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }


    @PatchMapping("/{userId}/role/{role}")
    public UserResponseDto changeRole(@PathVariable Long userId, @PathVariable UserRole role) {
        User user = userService.giveRole(userId, role);
        return new UserResponseDto(user);
    }

    @PatchMapping("/{userId}/deactivate")
    public UserResponseDto deactivate(@PathVariable Long userId) {
        User user = userService.deactivateUser(userId);
        return new UserResponseDto(user);
    }

    @PatchMapping("/{userId}/activate")
    public UserResponseDto activate(@PathVariable Long userId) {
        User user = userService.activateUser(userId);
        return new UserResponseDto(user);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/confirmed")
    public List<UserResponseDto> getConfirmedUsers() {
        return userService.getConfirmedUsers().stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{userId}/confirm-with-department")
    public ResponseEntity<UserResponseDto> confirmUserWithDepartment(
            @PathVariable Long userId, 
            @Valid @RequestBody ConfirmUserDto confirmUserDto) {
        try {
            User user = userService.confirmUserWithDepartment(userId, confirmUserDto.getDepartmentId());
            return ResponseEntity.ok(new UserResponseDto(user));
        } catch (Exception e) {
            log.error("Failed to confirm user with department: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUserAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UserAdminUpdateDto adminUpdateDto) {
        try {
            User user = userService.updateUserAdmin(userId, adminUpdateDto);
            return ResponseEntity.ok(new UserResponseDto(user));
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/bulk-update")
    public ResponseEntity<List<UserResponseDto>> bulkUpdateUsers(
            @Valid @RequestBody BulkUserUpdateDto bulkUpdateDto) {
        try {
            List<User> updatedUsers = userService.bulkUpdateUsers(bulkUpdateDto);
            List<UserResponseDto> response = updatedUsers.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to bulk update users: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/bulk-confirm")
    public ResponseEntity<Integer> bulkConfirmUsers(@RequestBody List<Long> userIds) {
        try {
            int confirmedCount = userService.bulkConfirmUsers(userIds);
            return ResponseEntity.ok(confirmedCount);
        } catch (Exception e) {
            log.error("Failed to bulk confirm users: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{doctorId}/assign-department")
    public ResponseEntity<UserResponseDto> assignDepartmentToDoctor(
            @PathVariable Long doctorId,
            @Valid @RequestBody ConfirmUserDto confirmUserDto) {
        try {
            User doctor = userService.assignDepartmentToDoctor(doctorId, confirmUserDto.getDepartmentId());
            return ResponseEntity.ok(new UserResponseDto(doctor));
        } catch (Exception e) {
            log.error("Failed to assign department to doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{doctorId}/change-department")
    public ResponseEntity<UserResponseDto> changeDoctorDepartment(
            @PathVariable Long doctorId,
            @Valid @RequestBody ConfirmUserDto confirmUserDto) {
        try {
            User doctor = userService.assignDepartmentToDoctor(doctorId, confirmUserDto.getDepartmentId());
            return ResponseEntity.ok(new UserResponseDto(doctor));
        } catch (Exception e) {
            log.error("Failed to change doctor department: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
