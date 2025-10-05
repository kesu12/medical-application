package by.bsuir.medical_application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signin")
    public String signin() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/patient-cabinet")
    public String patientCabinet() {
        return "patient-cabinet";
    }

    @GetMapping("/nurse-cabinet")
    public String nurseCabinet() {
        return "nurse-cabinet";
    }

    @GetMapping("/doctor-cabinet")
    public String doctorCabinet() {
        return "doctor-cabinet";
    }

    @GetMapping("/department-cabinet")
    public String departmentCabinet() {
        return "department-cabinet";
    }

    @GetMapping("/assignment-admin")
    public String assignmentAdmin() {
        return "assignment-admin";
    }
    
    @GetMapping("/medical-indicators-test")
    public String medicalIndicatorsTest() {
        return "medical-indicators-test";
    }
    
    @GetMapping("/realtime-monitoring")
    public String realtimeMonitoring() {
        return "realtime-monitoring";
    }

}