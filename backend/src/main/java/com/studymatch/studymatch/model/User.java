package com.studymatch.studymatch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Can be received in a request but is never sent back in a response
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean admin;


    /**
     * Empty constructor required by JPA.
     */
    public User() {}

    /**
     * Creates a user with the given credentials and admin flag.
     */
    public User(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }


    /**
     * Returns the user id.
     */
    public Long getIdUser() {
        return idUser;
    }

    /**
     * Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Tells whether this user is an admin.
     */
    public boolean isAdmin() {
        return admin;
    }
}
