package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByEmailAdmin(String emailAdmin);

}
