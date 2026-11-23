package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Returns the student with exactly this student number, or null if there is none.
     */
    Student findByNumberStudent(String numberStudent);

    /**
     * Returns the student with exactly this e-mail, or null if there is none.
     */
    Student findByEmailStudent(String emailStudent);

    /**
     * Returns the students whose name contains the given text, ignoring case.
     */
    List<Student> findByNameStudentContainingIgnoreCase(String name);

    /**
     * Returns the students that attend the course with the given id.
     */
    List<Student> findByCourse_IdCourse(Long idCourse);

    /**
     * Returns the students that enrolled in the given year.
     */
    List<Student> findByEnrollmentYear(Integer enrollmentYear);
}
