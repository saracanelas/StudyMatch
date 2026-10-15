package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Course findByCodeCourse(String codeCourse);

    List<Course> findByNameCourseContainingIgnoreCase(String name);

}

