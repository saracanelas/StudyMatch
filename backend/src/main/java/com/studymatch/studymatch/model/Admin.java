package com.studymatch.studymatch.model;

import jakarta.persistence.*;

@Entity
@Table(name="admins")

public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdmin;

    @Column(nullable = false, length = 100)
    private String nameAdmin;

    @Column(nullable = false, unique = true, length = 100)
    private String emailAdmin;

    @Column(nullable = false)
    private String passwordAdmin;

    public Admin() {}

    public Admin(Long idAdmin, String nameAdmin, String emailAdmin, String passwordAdmin){
        this.idAdmin = idAdmin;
        this.nameAdmin = nameAdmin;
        this.emailAdmin = emailAdmin;
        this.passwordAdmin = passwordAdmin;
    }

    public Long getIdAdmin() { return idAdmin; }

    public String getNameAdmin() { return nameAdmin; }

    public String getEmailAdmin() { return emailAdmin; }

    public String getPasswordAdmin() { return passwordAdmin; }

    public void setNameAdmin(String nameAdmin) { this.nameAdmin = nameAdmin; }

    public void setEmailAdmin(String emailAdmin) { this.emailAdmin = emailAdmin; }

    public void setPasswordAdmin(String passwordAdmin) { this.passwordAdmin = passwordAdmin; }
}