package by.bsuir.medical_application.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentCreateDto {

    private String departmentName;

    private String departmentDescription;

    public DepartmentCreateDto(String departmentName) {
        this.departmentName = departmentName;
    }

}
