package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.repository.AdminRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin createAdmin(Admin admin) {
        if (adminRepository.findByEmailAdmin(admin.getEmailAdmin()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An administrator with this email already exists."
            );
        }
        admin.setPasswordAdmin(passwordEncoder.encode(admin.getPasswordAdmin()));

        try {
            return adminRepository.save(admin);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "It was not possible to create the administrator."
            );
        }
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
}
