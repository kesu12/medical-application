package by.bsuir.medical_application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "indicators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Indicators {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "heartrate")
    private Integer heartrate;

    @Column(name = "temperature", precision = 4)
    private Double temperature;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "timestamp")
    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private User patient;

    /**
     * Проверяет, являются ли показатели критическими (требуют срочной медицинской помощи)
     * @return true если показатели критические
     */
    public boolean isCritical() {
        return (heartrate != null && (heartrate < 50 || heartrate > 130)) ||
               (temperature != null && (temperature < 35.0 || temperature > 38.1)) ||
               (spo2 != null && spo2 <= 90);
    }

    /**
     * Проверяет, требуют ли показатели внимания (но не критичны)
     * @return true если показатели требуют внимания
     */
    public boolean requiresAttention() {
        return (heartrate != null && ((heartrate >= 50 && heartrate < 60) || (heartrate > 100 && heartrate <= 120))) ||
               (temperature != null && temperature >= 37.1 && temperature <= 38.0) ||
               (spo2 != null && spo2 >= 91 && spo2 <= 94);
    }

    /**
     * Возвращает категорию показателей
     * @return строку с категорией: "Normal", "Requires Attention", "Critical", "Incompatible with Life"
     */
    public String getCategory() {
        // Проверяем на несовместимость с жизнью
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

    /**
     * Возвращает описание критичности показателей
     * @return строку с описанием критичности
     */
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
