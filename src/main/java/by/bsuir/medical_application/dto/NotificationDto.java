package by.bsuir.medical_application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String message;
    private String type;
    private Long patientId;
    private String patientName;
    private Long departmentId;
    private String departmentName;
    private Instant createdAt;
    private boolean read;
}
