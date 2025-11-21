package by.bsuir.medical_application.websocket;

import by.bsuir.medical_application.dto.MedicalIndicatorsDto;
import by.bsuir.medical_application.model.Indicators;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.repository.UserRepository;
import by.bsuir.medical_application.service.NotificationService;
import by.bsuir.medical_application.utils.PatientIndicatorsGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
public class MedicalIndicatorsWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService globalScheduler;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    private final Map<Long, Indicators> lastIndicators = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> activeMonitoring = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> monitoringTasks = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledExecutorService> patientExecutors = new ConcurrentHashMap<>();
    
    private final AtomicInteger threadCounter = new AtomicInteger(0);

    public MedicalIndicatorsWebSocketController(SimpMessagingTemplate messagingTemplate,
                                                NotificationService notificationService,
                                                UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.globalScheduler = Executors.newScheduledThreadPool(10);
    }

    
    @MessageMapping("/start-monitoring")
    @SendTo("/topic/medical-indicators")
    public void startMonitoring(Long patientId) {
        log.info("Starting real-time monitoring for patient: {}", patientId);
        
        stopMonitoringInternal(patientId);
        
        ScheduledExecutorService patientExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "monitoring-patient-" + patientId + "-" + threadCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        
        activeMonitoring.put(patientId, true);
        patientExecutors.put(patientId, patientExecutor);
        
        ScheduledFuture<?> task = patientExecutor.scheduleAtFixedRate(() -> {
            try {
                if (activeMonitoring.getOrDefault(patientId, false)) {
                    generateAndSendIndicators(patientId);
                } else {
                    ScheduledFuture<?> currentTask = monitoringTasks.get(patientId);
                    if (currentTask != null && !currentTask.isCancelled()) {
                        currentTask.cancel(false);
                    }
                }
            } catch (Exception e) {
                log.error("Error in monitoring task for patient {}: {}", patientId, e.getMessage(), e);
                stopMonitoringInternal(patientId);
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        monitoringTasks.put(patientId, task);
        
        log.info("Monitoring task created for patient {} with executor: {}", patientId, patientExecutor);
    }

    
    @MessageMapping("/stop-monitoring")
    public void stopMonitoring(Long patientId) {
        log.info("Stopping real-time monitoring for patient: {}", patientId);
        stopMonitoringInternal(patientId);
    }
    
    private void stopMonitoringInternal(Long patientId) {
        log.info("Stopping monitoring internally for patient: {}", patientId);
        
        activeMonitoring.put(patientId, false);
        
        ScheduledFuture<?> task = monitoringTasks.remove(patientId);
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            log.info("Cancelled monitoring task for patient: {}", patientId);
        }
        
        ScheduledExecutorService patientExecutor = patientExecutors.remove(patientId);
        if (patientExecutor != null) {
            try {
                patientExecutor.shutdown();
                if (!patientExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    log.warn("Patient executor for {} did not terminate gracefully, forcing shutdown", patientId);
                    patientExecutor.shutdownNow();
                    if (!patientExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                        log.error("Patient executor for {} did not terminate after forced shutdown", patientId);
                    }
                } else {
                    log.info("Patient executor for {} terminated gracefully", patientId);
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for patient executor termination for {}", patientId);
                patientExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        lastIndicators.remove(patientId);
        
        log.info("Monitoring completely stopped for patient: {}", patientId);
    }

    
    private void generateAndSendIndicators(Long patientId) {
        try {
            
            Indicators previousIndicators = lastIndicators.get(patientId);
            
            
            Indicators newIndicators = PatientIndicatorsGenerator.createStableIndicators(previousIndicators);
            newIndicators.setPatientId(patientId);
            
            
            lastIndicators.put(patientId, newIndicators);
            
           
            MedicalIndicatorsDto indicatorsDto = MedicalIndicatorsDto.builder()
                    .heartrate(newIndicators.getHeartrate())
                    .temperature(newIndicators.getTemperature())
                    .spo2(newIndicators.getSpo2())
                    .timestamp(newIndicators.getTimestamp())
                    .patientId(newIndicators.getPatientId())
                    .build();
            
            
            messagingTemplate.convertAndSend("/topic/medical-indicators/" + patientId, indicatorsDto);
            
           
            checkAndSendAlerts(indicatorsDto);
            
        } catch (Exception e) {
            log.error("Error generating indicators for patient {}: {}", patientId, e.getMessage());
        }
    }

    
    private void checkAndSendAlerts(MedicalIndicatorsDto indicators) {
        Indicators indicatorsObj = Indicators.builder()
                .temperature(indicators.getTemperature())
                .heartrate(indicators.getHeartrate())
                .spo2(indicators.getSpo2())
                .patientId(indicators.getPatientId())
                .build();
        
        String category = PatientIndicatorsGenerator.getCategory(indicatorsObj);
        
        if (!"Normal".equals(category)) {
            String warningMessage = PatientIndicatorsGenerator.getWarningMessage(indicatorsObj);
            
            Map<String, Object> alert = Map.of(
                    "type", "MEDICAL_WARNING",
                    "patientId", indicators.getPatientId(),
                    "category", category,
                    "alertLevel", "WARNING",
                    "message", warningMessage,
                    "indicators", indicators,
                    "timestamp", LocalDateTime.now()
            );
            
            sendAlertToDoctor(indicators.getPatientId(), alert);
            notificationService.notifyDoctorAboutIndicators(indicators.getPatientId(), category, warningMessage);
            
            log.warn("Medical warning sent to doctor for patient {}: {}", indicators.getPatientId(), warningMessage);
        }
    }

    
    private void sendAlertToDoctor(Long patientId, Map<String, Object> alert) {
        Long doctorId = getAssignedDoctorId(patientId);
        if (doctorId != null) {
            messagingTemplate.convertAndSend("/topic/doctor-alerts/" + doctorId, alert);
            log.info("Alert sent to doctor {} for patient {}", doctorId, patientId);
        }
    }

    
    private Long getAssignedDoctorId(Long patientId) {
        return userRepository.findById(patientId)
                .map(User::getAssignedDoctor)
                .map(User::getUserId)
                .orElse(null);
    }

   
    @MessageMapping("/send-test-indicators")
    public void sendTestIndicators(Long patientId) {
        log.info("Sending test indicators for patient: {}", patientId);
        
        Indicators testIndicators = PatientIndicatorsGenerator.createRandomIndicators(false); // Нормальные показатели
        testIndicators.setPatientId(patientId);
        
        lastIndicators.put(patientId, testIndicators);
        
        MedicalIndicatorsDto indicatorsDto = MedicalIndicatorsDto.builder()
                .heartrate(testIndicators.getHeartrate())
                .temperature(testIndicators.getTemperature())
                .spo2(testIndicators.getSpo2())
                .timestamp(testIndicators.getTimestamp())
                .patientId(testIndicators.getPatientId())
                .build();
        
        messagingTemplate.convertAndSend("/topic/medical-indicators/" + patientId, indicatorsDto);
        
        log.info("Test indicators sent for patient {}: Temp={}°C, HR={} bpm, SpO2={}%", 
                patientId, testIndicators.getTemperature(), testIndicators.getHeartrate(), testIndicators.getSpo2());
    }
    
    public void cleanupPatientResources(Long patientId) {
        log.info("Cleaning up resources for patient: {}", patientId);
        stopMonitoringInternal(patientId);
    }
    
    public void shutdown() {
        log.info("Shutting down WebSocket controller");
        
        for (Long patientId : monitoringTasks.keySet()) {
            stopMonitoringInternal(patientId);
        }
        
        for (Map.Entry<Long, ScheduledExecutorService> entry : patientExecutors.entrySet()) {
            Long patientId = entry.getKey();
            ScheduledExecutorService executor = entry.getValue();
            try {
                executor.shutdown();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("Forced shutdown of executor for patient: {}", patientId);
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        globalScheduler.shutdown();
        try {
            if (!globalScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                globalScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            globalScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("WebSocket controller shutdown completed");
    }
    
    public Map<String, Object> getThreadInfo() {
        Map<String, Object> info = new ConcurrentHashMap<>();
        
        info.put("activeMonitoringCount", activeMonitoring.size());
        info.put("activeMonitoringPatients", new ConcurrentHashMap<Long, Boolean>(activeMonitoring));
        
        info.put("monitoringTasksCount", monitoringTasks.size());
        info.put("monitoringTasksPatients", new ConcurrentHashMap<Long, Object>(monitoringTasks.keySet().stream().collect(java.util.stream.Collectors.toMap(k -> k, k -> "task"))));
        
        info.put("patientExecutorsCount", patientExecutors.size());
        info.put("patientExecutorsPatients", new ConcurrentHashMap<Long, Object>(patientExecutors.keySet().stream().collect(java.util.stream.Collectors.toMap(k -> k, k -> "executor"))));
        
        info.put("globalSchedulerShutdown", globalScheduler.isShutdown());
        info.put("globalSchedulerTerminated", globalScheduler.isTerminated());
        
        Thread[] allThreads = new Thread[Thread.activeCount()];
        Thread.enumerate(allThreads);
        int monitoringThreads = 0;
        for (Thread thread : allThreads) {
            if (thread != null && thread.getName().contains("monitoring-patient")) {
                monitoringThreads++;
            }
        }
        info.put("activeMonitoringThreads", monitoringThreads);
        info.put("totalActiveThreads", Thread.activeCount());
        
        return info;
    }
}