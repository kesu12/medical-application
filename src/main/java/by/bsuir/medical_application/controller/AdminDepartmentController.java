package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.model.Department;
import by.bsuir.medical_application.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/department")
@RequiredArgsConstructor
@Slf4j
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/all")
    public ResponseEntity<Iterable<Department>> getAllDepartments() {
        try {
            Iterable<Department> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            log.error("Failed to get all departments: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartment(@PathVariable Long id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            log.error("Failed to get department with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        try {
            Department created;
            if (department.getDescription() == null) {
                created = departmentService.createDepartment(department.getName());
            } else {
                created = departmentService.createDepartment(department.getName(), department.getDescription());
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create department: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        try {
            Department updated = departmentService.updateDepartment(id, department.getName(), department.getDescription());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to update department with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete department with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
