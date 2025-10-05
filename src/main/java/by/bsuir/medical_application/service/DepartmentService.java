package by.bsuir.medical_application.service;

import by.bsuir.medical_application.exceptions.DepartmentCreatingException;
import by.bsuir.medical_application.model.Department;
import by.bsuir.medical_application.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public Department getDepartmentByName(String name){
        return departmentRepository.findByName(name);
    }

    public Department getDepartmentById(Long id){
        return departmentRepository.findById(id).orElse(null);
    }

    public Department createDepartment(String departmentName) {
        if(departmentRepository.existsByName(departmentName)){
           throw new DepartmentCreatingException("Department already exists");
        }
        Department department = new Department();
        department.setName(departmentName);
        departmentRepository.save(department);
        return department;

    }
    public Department createDepartment(String departmentName, String departmentDescription) {
        if(departmentRepository.existsByName(departmentName)){
            throw new DepartmentCreatingException("Department already exists");
        }
        Department department = new Department();
        department.setName(departmentName);
        department.setDescription(departmentDescription);
        departmentRepository.save(department);
        return department;

    }

    public Iterable<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department updateDepartment(Long id, String name, String description) {
        Department existing = getDepartmentById(id);
        if (existing == null) {
            throw new DepartmentCreatingException("Department not found with id: " + id);
        }
        
        log.info("Updating department {} with name: {}, description: {}", id, name, description);
        
        if (name != null) existing.setName(name);
        if (description != null) existing.setDescription(description);
        
        Department updated = departmentRepository.save(existing);
        log.info("Department {} updated successfully", updated.getId());
        
        return updated;
    }

    public void deleteDepartment(Long id) {
        Department existing = getDepartmentById(id);
        departmentRepository.delete(existing);
    }
}
