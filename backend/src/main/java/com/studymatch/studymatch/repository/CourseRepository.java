package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Returns the course with exactly this code, or null if there is none.
     */
    Course findByCodeCourse(String codeCourse);

    /**
     * Returns the courses whose name contains the given text, ignoring case.
     */
    List<Course> findByNameCourseContainingIgnoreCase(String name);

}

