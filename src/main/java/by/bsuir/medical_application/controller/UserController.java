package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.dto.UserUpdateDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updateUserPassword(@PathVariable("id") Long id, @RequestBody UserUpdateDto userUpdateDto){

        User user = userService.updateUserPassword(id, userUpdateDto);
        return ResponseEntity.ok(new UserResponseDto(user));
    }
}
