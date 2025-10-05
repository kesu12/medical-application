package by.bsuir.medical_application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalIndicatorsDto {
    
    @NotNull(message = "Heart rate is required")
    @Min(value = 30, message = "Heart rate can't be less than 30 bpm")
    @Max(value = 200, message = "Heart rate can't be more than 200 bpm")
    private Integer heartrate;
    
    @NotNull(message = "Temperature is required")
    @DecimalMin(value = "30.0", message = "Temperature can't be less than 30.0°C")
    @DecimalMax(value = "45.0", message = "Temperature can't be more than 45.0°C")
    @Digits(integer = 2, fraction = 1, message = "Temperature must have at most 1 decimal place")
    private Double temperature;
    
    @NotNull(message = "SpO2 is required")
    @Min(value = 70, message = "SpO2 can't be less than 70%")
    @Max(value = 100, message = "SpO2 can't be more than 100%")
    private Integer spo2;
    
    private LocalDateTime timestamp;
    private Long patientId;
    
    public boolean isCritical() {
        return (heartrate != null && (heartrate < 50 || heartrate > 130)) ||
               (temperature != null && (temperature < 35.0 || temperature > 38.1)) ||
               (spo2 != null && spo2 <= 90);
    }
    
    public boolean requiresAttention() {
        return (heartrate != null && ((heartrate >= 50 && heartrate < 60) || (heartrate > 100 && heartrate <= 120))) ||
               (temperature != null && temperature >= 37.1 && temperature <= 38.0) ||
               (spo2 != null && spo2 >= 91 && spo2 <= 94);
    }
    
    public String getCategory() {
        if ((heartrate != null && (heartrate == 0 || heartrate > 250)) ||
            (temperature != null && (temperature > 41.5 || temperature < 28.0)) ||
            (spo2 != null && spo2 < 70)) {
            return "Incompatible with Life";
        }
        
        if (isCritical()) {
            return "Critical";
        }
        
        if (requiresAttention()) {
            return "Requires Attention";
        }
        
        return "Normal";
    }
    
    public String getCriticalStatus() {
        String category = getCategory();
        
        if ("Normal".equals(category)) {
            return "Normal";
        }
        
        StringBuilder status = new StringBuilder();
        
        if ("Incompatible with Life".equals(category)) {
            status.append("INCOMPATIBLE WITH LIFE - ");
        } else if ("Critical".equals(category)) {
            status.append("CRITICAL - ");
        } else {
            status.append("REQUIRES ATTENTION - ");
        }
        
        if (heartrate != null) {
            if (heartrate == 0 || heartrate > 250) {
                status.append("Incompatible heart rate (").append(heartrate).append(" bpm). ");
            } else if (heartrate < 50 || heartrate > 130) {
                status.append("Critical heart rate (").append(heartrate).append(" bpm). ");
            } else if ((heartrate >= 50 && heartrate < 60) || (heartrate > 100 && heartrate <= 120)) {
                status.append("Elevated heart rate (").append(heartrate).append(" bpm). ");
            }
        }
        
        if (temperature != null) {
            if (temperature > 41.5 || temperature < 28.0) {
                status.append("Incompatible temperature (").append(temperature).append("°C). ");
            } else if (temperature < 35.0 || temperature > 38.1) {
                status.append("Critical temperature (").append(temperature).append("°C). ");
            } else if (temperature >= 37.1 && temperature <= 38.0) {
                status.append("Elevated temperature (").append(temperature).append("°C). ");
            }
        }
        
        if (spo2 != null) {
            if (spo2 < 70) {
                status.append("Incompatible SpO2 (").append(spo2).append("%). ");
            } else if (spo2 <= 90) {
                status.append("Critical SpO2 (").append(spo2).append("%). ");
            } else if (spo2 >= 91 && spo2 <= 94) {
                status.append("Low SpO2 (").append(spo2).append("%). ");
            }
        }
        
        return status.toString().trim();
    }
}
