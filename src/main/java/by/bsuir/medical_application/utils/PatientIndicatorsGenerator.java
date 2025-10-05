package by.bsuir.medical_application.utils;

import by.bsuir.medical_application.model.Indicators;
import java.time.LocalDateTime;
import java.util.Random;

public class PatientIndicatorsGenerator {

    public static Indicators createRandomIndicators(){
        Random random = new Random();
        
        int spo2 = 80 + random.nextInt(21);
        
        int heartrate = 30 + random.nextInt(221);
        
        double temperature = 30.0 + random.nextDouble() * 11.0;
        
        return Indicators.builder()
                .heartrate(heartrate)
                .temperature(Math.round(temperature * 10.0) / 10.0)
                .spo2(spo2)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static Indicators createRandomIndicators(boolean includeWarningValues) {
        Random random = new Random();
        
        int heartrate;
        double temperature;
        int spo2;
        
        if (includeWarningValues && random.nextDouble() < 0.4) {
            spo2 = 80 + random.nextInt(17);
            
            if (random.nextBoolean()) {
                heartrate = random.nextBoolean() ? 
                    30 + random.nextInt(31) :
                    100 + random.nextInt(151);
            } else {
                heartrate = random.nextBoolean() ? 
                    30 + random.nextInt(31) :
                    100 + random.nextInt(151);
            }
                
            if (random.nextBoolean()) {
                temperature = random.nextBoolean() ?
                    30.0 + random.nextDouble() * 5.0 :
                    37.0 + random.nextDouble() * 4.0;
            } else {
                temperature = random.nextBoolean() ?
                    30.0 + random.nextDouble() * 5.0 :
                    37.0 + random.nextDouble() * 4.0;
            }
        } else {
            spo2 = 96 + random.nextInt(5);
            
            heartrate = 60 + random.nextInt(41);
            
            temperature = 35.0 + random.nextDouble() * 2.0;
        }
        
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
        
        double temperature = previousIndicators.getTemperature() + (random.nextDouble() - 0.5) * 0.2;
        
        int heartrate = previousIndicators.getHeartrate() + random.nextInt(41) - 20;
        
        int spo2 = previousIndicators.getSpo2() + random.nextInt(3) - 1;
        
        temperature = Math.max(30.0, Math.min(41.0, temperature));
        
        heartrate = Math.max(30, Math.min(250, heartrate));
        
        spo2 = Math.max(80, Math.min(100, spo2));
        
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