package com.studymatch.studymatch.dto;

import com.studymatch.studymatch.model.Admin;

/**
 * Administrator data returned to the client. It must never include the password.
 */
public record AdminResponse(Long idAdmin, String nameAdmin, String emailAdmin) {

    /**
     * Builds the response from an administrator entity.
     */
    public static AdminResponse from(Admin admin) {
        return new AdminResponse(admin.getIdAdmin(), admin.getNameAdmin(), admin.getEmailAdmin());
    }
}
