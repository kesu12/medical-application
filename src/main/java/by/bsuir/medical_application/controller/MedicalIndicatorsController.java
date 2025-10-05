package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.MedicalIndicatorsDto;
import by.bsuir.medical_application.model.Indicators;
import by.bsuir.medical_application.utils.PatientIndicatorsGenerator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-indicators")
@Slf4j
public class MedicalIndicatorsController {

    /**
     * Принимает медицинские показатели от мобильного приложения
     * @param indicatorsDto данные показателей
     * @return ответ с результатом обработки
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitIndicators(
            @Valid @RequestBody MedicalIndicatorsDto indicatorsDto) {
        
        log.info("Received medical indicators: heartrate={}, temperature={}, spo2={}, patientId={}", 
                indicatorsDto.getHeartrate(), indicatorsDto.getTemperature(), 
                indicatorsDto.getSpo2(), indicatorsDto.getPatientId());
        
        if (indicatorsDto.getTimestamp() == null) {
            indicatorsDto.setTimestamp(LocalDateTime.now());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Medical indicators received successfully");
        response.put("timestamp", indicatorsDto.getTimestamp());
        response.put("patientId", indicatorsDto.getPatientId());
        
        String category = indicatorsDto.getCategory();
        response.put("category", category);
        response.put("criticalStatus", indicatorsDto.getCriticalStatus());
        
        if ("Incompatible with Life".equals(category)) {
            response.put("alert", true);
            response.put("alertLevel", "EMERGENCY");
            response.put("message", "INCOMPATIBLE WITH LIFE! " + indicatorsDto.getCriticalStatus());
            log.error("INCOMPATIBLE WITH LIFE indicators detected for patient {}: {}", 
                    indicatorsDto.getPatientId(), indicatorsDto.getCriticalStatus());
        } else if ("Critical".equals(category)) {
            response.put("alert", true);
            response.put("alertLevel", "CRITICAL");
            response.put("message", "Critical indicators detected! " + indicatorsDto.getCriticalStatus());
            log.warn("Critical medical indicators detected for patient {}: {}", 
                    indicatorsDto.getPatientId(), indicatorsDto.getCriticalStatus());
        } else if ("Requires Attention".equals(category)) {
            response.put("alert", true);
            response.put("alertLevel", "WARNING");
            response.put("message", "Indicators require attention: " + indicatorsDto.getCriticalStatus());
            log.info("Medical indicators require attention for patient {}: {}", 
                    indicatorsDto.getPatientId(), indicatorsDto.getCriticalStatus());
        } else {
            response.put("alert", false);
            response.put("alertLevel", "NORMAL");
            response.put("message", "Normal indicators received");
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * Генерирует случайные медицинские показатели для тестирования
     * @param includeCritical включить ли критические значения
     * @return случайные показатели
     */
    @GetMapping("/generate-random")
    public ResponseEntity<MedicalIndicatorsDto> generateRandomIndicators(
            @RequestParam(defaultValue = "false") boolean includeCritical) {
        
        Indicators indicators = PatientIndicatorsGenerator.createRandomIndicators(includeCritical);
        
        MedicalIndicatorsDto dto = MedicalIndicatorsDto.builder()
                .heartrate(indicators.getHeartrate())
                .temperature(indicators.getTemperature())
                .spo2(indicators.getSpo2())
                .timestamp(indicators.getTimestamp())
                .patientId(indicators.getPatientId())
                .build();
        
        log.info("Generated random indicators: heartrate={}, temperature={}, spo2={}", 
                dto.getHeartrate(), dto.getTemperature(), dto.getSpo2());
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Получает последние показатели пациента (заглушка для демонстрации)
     * @param patientId ID пациента
     * @return последние показатели
     */
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<MedicalIndicatorsDto> getLatestIndicators(@PathVariable Long patientId) {
        
        Indicators indicators = PatientIndicatorsGenerator.createRandomIndicators();
        indicators.setPatientId(patientId);
        
        MedicalIndicatorsDto dto = MedicalIndicatorsDto.builder()
                .heartrate(indicators.getHeartrate())
                .temperature(indicators.getTemperature())
                .spo2(indicators.getSpo2())
                .timestamp(indicators.getTimestamp())
                .patientId(indicators.getPatientId())
                .build();
        
        log.info("Retrieved latest indicators for patient {}: heartrate={}, temperature={}, spo2={}", 
                patientId, dto.getHeartrate(), dto.getTemperature(), dto.getSpo2());
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Проверяет статус здоровья пациента на основе показателей
     * @param indicatorsDto показатели для анализа
     * @return анализ состояния здоровья
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeIndicators(
            @Valid @RequestBody MedicalIndicatorsDto indicatorsDto) {
        
        Map<String, Object> analysis = new HashMap<>();
        
        String heartrateStatus = analyzeHeartrate(indicatorsDto.getHeartrate());
        analysis.put("heartrateStatus", heartrateStatus);
        
        String temperatureStatus = analyzeTemperature(indicatorsDto.getTemperature());
        analysis.put("temperatureStatus", temperatureStatus);
        
        String spo2Status = analyzeSpo2(indicatorsDto.getSpo2());
        analysis.put("spo2Status", spo2Status);
        
        String overallStatus = determineOverallStatus(indicatorsDto);
        analysis.put("overallStatus", overallStatus);
        analysis.put("category", indicatorsDto.getCategory());
        analysis.put("isCritical", indicatorsDto.isCritical());
        analysis.put("requiresAttention", indicatorsDto.requiresAttention());
        analysis.put("recommendations", generateRecommendations(indicatorsDto));
        
        log.info("Analyzed indicators for patient {}: overall status = {}", 
                indicatorsDto.getPatientId(), overallStatus);
        
        return ResponseEntity.ok(analysis);
    }
    
    private String analyzeHeartrate(Integer heartrate) {
        if (heartrate == null) return "No data";
        if (heartrate == 0 || heartrate > 250) return "Incompatible with Life";
        if (heartrate < 50) return "Critical - Bradycardia";
        if (heartrate < 60) return "Requires Attention - Low";
        if (heartrate <= 100) return "Normal";
        if (heartrate <= 120) return "Requires Attention - Elevated";
        if (heartrate <= 130) return "Critical - Tachycardia";
        return "Critical - Severe Tachycardia";
    }
    
    private String analyzeTemperature(Double temperature) {
        if (temperature == null) return "No data";
        if (temperature > 41.5 || temperature < 28.0) return "Incompatible with Life";
        if (temperature < 35.0) return "Critical - Hypothermia";
        if (temperature < 36.1) return "Critical - Low";
        if (temperature <= 37.0) return "Normal";
        if (temperature <= 38.0) return "Requires Attention - Elevated";
        if (temperature <= 38.1) return "Critical - Hyperthermia";
        return "Critical - Severe Hyperthermia";
    }
    
    private String analyzeSpo2(Integer spo2) {
        if (spo2 == null) return "No data";
        if (spo2 < 70) return "Incompatible with Life";
        if (spo2 <= 90) return "Critical - Severe hypoxia";
        if (spo2 <= 94) return "Requires Attention - Low";
        return "Normal";
    }
    
    private String determineOverallStatus(MedicalIndicatorsDto indicators) {
        String category = indicators.getCategory();
        
        switch (category) {
            case "Incompatible with Life":
                return "INCOMPATIBLE WITH LIFE - Immediate emergency response required";
            case "Critical":
                return "CRITICAL - Immediate medical attention required";
            case "Requires Attention":
                return "REQUIRES ATTENTION - Monitor closely and consider medical consultation";
            default:
                return "NORMAL - All indicators within normal range";
        }
    }
    
    private String generateRecommendations(MedicalIndicatorsDto indicators) {
        StringBuilder recommendations = new StringBuilder();
        String category = indicators.getCategory();
        
        switch (category) {
            case "Incompatible with Life":
                recommendations.append("• EMERGENCY: Call Emergency Service immediately! ");
                recommendations.append("• Begin emergency life support procedures. ");
                recommendations.append("• Prepare for emergency medical transport. ");
                break;
                
            case "Critical":
                recommendations.append("• SEEK IMMEDIATE MEDICAL ATTENTION. ");
                recommendations.append("• Do not delay - go to emergency room. ");
                if (indicators.getHeartrate() != null && (indicators.getHeartrate() < 50 || indicators.getHeartrate() > 130)) {
                    recommendations.append("• Monitor heart rate continuously. ");
                }
                if (indicators.getTemperature() != null && (indicators.getTemperature() < 35.0 || indicators.getTemperature() > 38.1)) {
                    recommendations.append("• Monitor temperature and provide appropriate cooling/heating. ");
                }
                if (indicators.getSpo2() != null && indicators.getSpo2() <= 90) {
                    recommendations.append("• Provide supplemental oxygen if available. ");
                }
                break;
                
            case "Requires Attention":
                recommendations.append("• Monitor closely and consider medical consultation. ");
                if (indicators.getHeartrate() != null && ((indicators.getHeartrate() >= 50 && indicators.getHeartrate() < 60) || (indicators.getHeartrate() > 100 && indicators.getHeartrate() <= 120))) {
                    recommendations.append("• Rest and avoid strenuous activity. ");
                }
                if (indicators.getTemperature() != null && indicators.getTemperature() >= 37.1 && indicators.getTemperature() <= 38.0) {
                    recommendations.append("• Stay hydrated and rest. Consider fever-reducing medication. ");
                }
                if (indicators.getSpo2() != null && indicators.getSpo2() >= 91 && indicators.getSpo2() <= 94) {
                    recommendations.append("• Ensure adequate ventilation. Monitor breathing. ");
                }
                break;
                
            default:
                recommendations.append("• Continue monitoring. All indicators normal. ");
                recommendations.append("• Maintain healthy lifestyle and regular check-ups. ");
                break;
        }
        
        return recommendations.toString().trim();
    }
}

