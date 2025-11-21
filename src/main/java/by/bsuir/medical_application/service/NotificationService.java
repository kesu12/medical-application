package by.bsuir.medical_application.service;

import by.bsuir.medical_application.dto.NotificationDto;
import by.bsuir.medical_application.model.Notification;
import by.bsuir.medical_application.model.NotificationType;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.repository.NotificationRepository;
import by.bsuir.medical_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationDto> getNotificationsForUser(Long userId) {
        return notificationRepository.findTop50ByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<NotificationDto> getNotificationsForDoctor(Long doctorId) {
        return getNotificationsForUser(doctorId);
    }

    public List<NotificationDto> getNotificationsForNurse(Long nurseId) {
        return getNotificationsForUser(nurseId);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return;
        }
        Notification notification = notificationOpt.get();
        if (!Objects.equals(notification.getRecipient().getUserId(), userId)) {
            log.warn("User {} attempted to change notification {} read status", userId, notificationId);
            return;
        }
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserIdAndReadFalse(userId);
        if (unreadNotifications.isEmpty()) {
            return;
        }
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public void notifyPatientTreatmentUpdated(User patient) {
        if (patient == null) {
            return;
        }
        User doctor = patient.getAssignedDoctor();
        String doctorName = resolveFullName(doctor);
        String message = doctorName != null
                ? "Ваш план лечения обновлён врачом " + doctorName
                : "Ваш план лечения был обновлён";
        createNotification(patient, NotificationType.PATIENT_TREATMENT_UPDATED, message, patient);

        User nurse = patient.getAssignedNurse();
        if (nurse != null) {
            String nurseMessage = "Лечение пациента " + resolveFullName(patient) + " было обновлено";
            createNotification(nurse, NotificationType.NURSE_PATIENT_TREATMENT_UPDATED, nurseMessage, patient);
        }
    }

    public void notifyPatientAssignedDepartment(User patient) {
        if (patient == null) {
            return;
        }
        String departmentName = patient.getDepartment() != null ? patient.getDepartment().getName() : "не назначен";
        String message = "Вам назначено отделение: " + departmentName;
        createNotification(patient, NotificationType.PATIENT_DEPARTMENT_ASSIGNED, message, patient);

        if (patient.getAssignedDoctor() == null && patient.getDepartment() != null) {
            notifyDoctorsAboutPatientWithoutDoctor(patient);
        }
    }

    public void notifyPatientAssignedDoctor(User patient) {
        if (patient == null || patient.getAssignedDoctor() == null) {
            return;
        }
        String doctorName = resolveFullName(patient.getAssignedDoctor());
        String message = "Вам назначен врач " + doctorName;
        createNotification(patient, NotificationType.PATIENT_DOCTOR_ASSIGNED, message, patient);
    }

    public void notifyPatientAssignedNurse(User patient) {
        if (patient == null || patient.getAssignedNurse() == null) {
            return;
        }
        String nurseName = resolveFullName(patient.getAssignedNurse());
        String message = "С вами теперь работает медсестра " + nurseName;
        createNotification(patient, NotificationType.PATIENT_NURSE_ASSIGNED, message, patient);
        if (patient.getDepartment() == null) {
            notifyNurseAboutPatientWithoutDepartment(patient.getAssignedNurse(), patient);
        }
    }

    public void notifyDoctorsAboutPatientWithoutDoctor(User patient) {
        if (patient == null || patient.getDepartment() == null) {
            return;
        }
        List<User> doctors = userRepository.findByDepartmentIdAndRole(patient.getDepartment().getId(), UserRole.DOCTOR);
        if (doctors.isEmpty()) {
            return;
        }
        String patientName = resolveFullName(patient);
        String deptName = patient.getDepartment().getName();
        String message = "Пациент " + patientName + " в отделении " + deptName + " пока без лечащего врача";
        doctors.forEach(doctor -> createNotification(doctor, NotificationType.DOCTOR_PATIENT_WITHOUT_DOCTOR, message, patient));
    }

    public void notifyDoctorAboutIndicators(Long patientId, String category, String warning) {
        Optional<User> patientOpt = userRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return;
        }
        User patient = patientOpt.get();
        User doctor = patient.getAssignedDoctor();
        if (doctor == null) {
            return;
        }
        String patientName = resolveFullName(patient);
        StringBuilder message = new StringBuilder("Показатели пациента ")
                .append(patientName)
                .append(" ухудшились (").append(category).append(")");
        if (warning != null && !warning.isBlank()) {
            message.append(": ").append(warning);
        }
        createNotification(doctor, NotificationType.DOCTOR_PATIENT_INDICATOR_ALERT, message.toString(), patient);
    }

    public void notifyAccountSecurityUpdate(User user, String details) {
        if (user == null) {
            return;
        }
        String message = details != null && !details.isBlank()
                ? details
                : "Настройки вашей учётной записи были обновлены";
        createNotification(user, NotificationType.ACCOUNT_SECURITY, message, null);
    }

    public void notifyNurseAboutPatientWithoutDepartment(User nurse, User patient) {
        if (nurse == null || patient == null || patient.getDepartment() != null) {
            return;
        }
        String message = "У пациента " + resolveFullName(patient) + " отсутствует назначенное отделение";
        createNotification(nurse, NotificationType.NURSE_PATIENT_WITHOUT_DEPARTMENT, message, patient);
    }

    private void createNotification(User recipient,
                                    NotificationType type,
                                    String message,
                                    User patientContext) {
        if (recipient == null) {
            return;
        }
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .patientId(patientContext != null ? patientContext.getUserId() : null)
                .patientName(patientContext != null ? resolveFullName(patientContext) : null)
                .departmentId(patientContext != null && patientContext.getDepartment() != null
                        ? patientContext.getDepartment().getId() : null)
                .departmentName(patientContext != null && patientContext.getDepartment() != null
                        ? patientContext.getDepartment().getName() : null)
                        .read(false)
                        .build();
        notificationRepository.save(notification);
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getUserId())
                .message(notification.getMessage())
                .type(notification.getType())
                .patientId(notification.getPatientId())
                .patientName(notification.getPatientName())
                .departmentId(notification.getDepartmentId())
                .departmentName(notification.getDepartmentName())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .build();
    }

    private String resolveFullName(User user) {
        if (user == null) {
            return "без имени";
        }
        StringBuilder fullName = new StringBuilder();
        if (user.getFirstName() != null) {
            fullName.append(user.getFirstName());
        }
        if (user.getLastName() != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(user.getLastName());
        }
        if (fullName.length() == 0) {
            return user.getUsername();
        }
        return fullName.toString().trim();
    }
}
