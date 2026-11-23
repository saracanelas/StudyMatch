package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.AdminRequest;
import com.studymatch.studymatch.dto.AdminResponse;
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

    public AdminResponse createAdmin(AdminRequest request) {
        if (adminRepository.findByEmailAdmin(request.emailAdmin()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An administrator with this email already exists."
            );
        }
        Admin admin = new Admin(null, request.nameAdmin(), request.emailAdmin(),
                passwordEncoder.encode(request.passwordAdmin()));

        try {
            return AdminResponse.from(adminRepository.save(admin));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "It was not possible to create the administrator."
            );
        }
    }

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream().map(AdminResponse::from).toList();
    }
}
