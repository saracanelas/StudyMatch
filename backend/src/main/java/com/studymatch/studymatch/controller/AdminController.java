package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.dto.AdminRequest;
import com.studymatch.studymatch.dto.AdminResponse;
import com.studymatch.studymatch.service.AdminService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminResponse createAdmin(@Valid @RequestBody AdminRequest admin) {
        return adminService.createAdmin(admin);
    }

    @GetMapping
    public List<AdminResponse> getAllAdmins() {
        return adminService.getAllAdmins();
    }
}
