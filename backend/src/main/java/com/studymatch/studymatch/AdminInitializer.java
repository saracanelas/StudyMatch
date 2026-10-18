package com.studymatch.studymatch;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            AdminRepository adminRepository,
            @Value("${admin.name:Administrator}") String adminName,
            @Value("${admin.email:}") String adminEmail,
            @Value("${admin.password:}") String adminPassword) {
        this.adminRepository = adminRepository;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void initializeAdmin() {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        if (adminRepository.findByEmailAdmin(adminEmail) != null) {
            return;
        }

        Admin admin = new Admin();
        admin.setNameAdmin(adminName);
        admin.setEmailAdmin(adminEmail);
        admin.setPasswordAdmin(passwordEncoder.encode(adminPassword));
        adminRepository.save(admin);
    }
}
