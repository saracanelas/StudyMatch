package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Returns the user with exactly this username, or null if there is none.
     */
    User findByUsername(String username);

}
