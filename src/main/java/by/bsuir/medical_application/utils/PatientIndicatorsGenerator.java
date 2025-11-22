package by.bsuir.medical_application.utils;

import by.bsuir.medical_application.model.Indicators;
import java.time.LocalDateTime;
import java.util.Random;

public class PatientIndicatorsGenerator {

    public static Indicators createRandomIndicators(){
        Random random = new Random();
        
        // SpO2: 98-100%
        int spo2 = 98 + random.nextInt(3);
        
        // Heartrate: 60-100 bpm
        int heartrate = 60 + random.nextInt(41);
        
        // Temperature: 36.0-36.9°C
        double temperature = 36.0 + random.nextDouble() * 0.9;
        
        return Indicators.builder()
                .heartrate(heartrate)
                .temperature(Math.round(temperature * 10.0) / 10.0)
                .spo2(spo2)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static Indicators createRandomIndicators(boolean includeWarningValues) {
        Random random = new Random();
        
        // Always generate normal values regardless of includeWarningValues parameter
        // SpO2: 95-100%
        int spo2 = 98 + random.nextInt(3);

        // Heartrate: 60-100 bpm
        int heartrate = 60 + random.nextInt(41);
        
        // Temperature: 36.0-36.9°C
        double temperature = 36.0 + random.nextDouble() * 0.9;
        
        return Indicators.builder()
                .heartrate(heartrate)
                .temperature(Math.round(temperature * 10.0) / 10.0)
                .spo2(spo2)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static Indicators createStableIndicators(Indicators previousIndicators) {
        Random random = new Random();
        
        if (previousIndicators == null) {
            return createRandomIndicators();
        }
        
        // Generate stable indicators with small variations, but always within normal range
        // Temperature: 36.0-36.9°C with small variation (±0.1°C)
        double temperature = previousIndicators.getTemperature() + (random.nextDouble() - 0.5) * 0.2;
        temperature = Math.max(36.0, Math.min(36.9, temperature));
        
        // Heartrate: 60-100 bpm with small variation (±5 bpm)
        int heartrate = previousIndicators.getHeartrate() + random.nextInt(11) - 5;
        heartrate = Math.max(60, Math.min(100, heartrate));
        
        // SpO2: 95-100% with small variation (±1%)
        int spo2 = previousIndicators.getSpo2() + random.nextInt(3) - 1;
        spo2 = Math.max(98, Math.min(100, spo2));
        
        return Indicators.builder()
                .heartrate(heartrate)
                .temperature(Math.round(temperature * 10.0) / 10.0)
                .spo2(spo2)
                .timestamp(LocalDateTime.now())
                .patientId(previousIndicators.getPatientId())
                .build();
    }

    public static String getCategory(Indicators indicators) {
        double temperature = indicators.getTemperature();
        int heartrate = indicators.getHeartrate();
        int spo2 = indicators.getSpo2();
        
        boolean temperatureNormal = temperature >= 35.0 && temperature <= 37.0;
        
        boolean heartrateNormal = heartrate >= 60 && heartrate <= 100;
        
        boolean spo2Normal = spo2 >= 96 && spo2 <= 100;
        
        if (temperatureNormal && heartrateNormal && spo2Normal) {
            return "Normal";
        }
        
        return "Warning";
    }
    
    public static String getWarningMessage(Indicators indicators) {
        if ("Normal".equals(getCategory(indicators))) {
            return null;
        }
        
        StringBuilder warning = new StringBuilder();
        double temperature = indicators.getTemperature();
        int heartrate = indicators.getHeartrate();
        int spo2 = indicators.getSpo2();
        
        if (temperature < 35.0) {
            warning.append("Низкая температура: ").append(temperature).append("°C (норма: 35.0-37.0°C). ");
        } else if (temperature > 37.0) {
            warning.append("Высокая температура: ").append(temperature).append("°C (норма: 35.0-37.0°C). ");
        }
        
        if (heartrate < 60) {
            warning.append("Низкий пульс: ").append(heartrate).append(" уд/мин (норма: 60-100 уд/мин). ");
        } else if (heartrate > 100) {
            warning.append("Высокий пульс: ").append(heartrate).append(" уд/мин (норма: 60-100 уд/мин). ");
        }
        
        if (spo2 < 96) {
            warning.append("Низкий SpO2: ").append(spo2).append("% (норма: 96-100%). ");
        }
        
        return warning.toString().trim();
    }
    
    public static boolean requiresDoctorNotification(Indicators indicators) {
        return "Warning".equals(getCategory(indicators));
    }

}