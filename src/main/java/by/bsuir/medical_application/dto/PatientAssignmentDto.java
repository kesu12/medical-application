package by.bsuir.medical_application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAssignmentDto {
    private Long patientId;
    private Long nurseId;
    private Long doctorId;
    private Long departmentId;
}
