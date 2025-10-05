package by.bsuir.medical_application.repository;

import by.bsuir.medical_application.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);


    boolean existsByName(String name);
}
