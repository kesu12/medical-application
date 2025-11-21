package by.bsuir.medical_application.dto;

import by.bsuir.medical_application.model.NotificationType;
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
    private Long recipientId;
    private String message;
    private NotificationType type;
    private Long patientId;
    private String patientName;
    private Long departmentId;
    private String departmentName;
    private Instant createdAt;
    private boolean read;
}
