package by.bsuir.medical_application.repository;

import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username,
                                    @Param("email") String email);

    User findByUsername(String username);
    User findByEmail(String email);

    User getUserByUserId(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    List<User> findByRole(UserRole role);


    List<User> findByRoleAndConfirmed(UserRole role, Boolean confirmed);
    
    List<User> findByAssignedNurseUserId(Long nurseId);
    
    List<User> findByAssignedDoctorUserId(Long doctorId);
    
    List<User> findByRoleAndAssignedDoctorIsNull(UserRole role);
    
    List<User> findByDepartmentIdAndRole(Long departmentId, UserRole role);
    
    List<User> findByDepartmentIdAndRoleAndAssignedDoctorIsNull(Long departmentId, UserRole role);
}
