package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        Admin admin = adminRepository.findByEmailAdmin(email);

        if (admin == null) {
            return false;
        }

        return passwordEncoder.matches(password, admin.getPasswordAdmin());
    }
}
