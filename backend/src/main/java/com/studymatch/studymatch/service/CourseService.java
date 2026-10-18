package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.repository.CourseRepository;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CurricularUnitRepository curricularUnitRepository;

    /**
     * Receives the repositories used to read and save courses and curricular units.
     */
    public CourseService(CourseRepository courseRepository, CurricularUnitRepository curricularUnitRepository) {
        this.courseRepository = courseRepository;
        this.curricularUnitRepository = curricularUnitRepository;
    }

    /**
     * Returns every course in the database.
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Returns the course with the given id, or 404 if it does not exist.
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return findCourseOrThrow(id);
    }

    /**
     * Saves a new course, refusing it if another course already uses the same code.
     */
    @Transactional
    public Course createCourse(Course newCourse) {
        if (courseRepository.findByCodeCourse(newCourse.getCodeCourse()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um curso com o código " + newCourse.getCodeCourse());
        }
        Course course = new Course(null, newCourse.getCodeCourse(), newCourse.getNameCourse());
        return courseRepository.save(course);
    }

    /**
     * Changes the code and name of an existing course, keeping codes unique.
     */
    @Transactional
    public Course updateCourse(Long id, Course updatedCourse) {
        Course course = findCourseOrThrow(id);
        Course courseWithSameCode = courseRepository.findByCodeCourse(updatedCourse.getCodeCourse());
        if (courseWithSameCode != null && !courseWithSameCode.getIdCourse().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um curso com o código " + updatedCourse.getCodeCourse());
        }
        course.setCodeCourse(updatedCourse.getCodeCourse());
        course.setNameCourse(updatedCourse.getNameCourse());
        return course; // saved automatically when the transaction ends
    }

    /**
     * Replaces the curricular units of a course with the given list of unit ids,
     * adding the new ones and removing the ones that are no longer wanted.
     */
    @Transactional
    public Course setCourseUnits(Long id, List<Long> unitIds) {
        Course course = findCourseOrThrow(id);

        Set<Long> wantedUnitIds = new HashSet<>(unitIds);
        List<CurricularUnit> wantedUnits = curricularUnitRepository.findAllById(wantedUnitIds);
        if (wantedUnits.size() != wantedUnitIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uma ou mais UCs indicadas não existem");
        }

        course.getCurricularUnits().stream()
                .filter(unit -> !wantedUnitIds.contains(unit.getIdUnit()))
                .toList()
                .forEach(course::removeCurricularUnit);

        Set<Long> currentUnitIds = course.getCurricularUnits().stream()
                .map(CurricularUnit::getIdUnit)
                .collect(Collectors.toSet());
        wantedUnits.stream()
                .filter(unit -> !currentUnitIds.contains(unit.getIdUnit()))
                .forEach(course::addCurricularUnit);

        return course;
    }

    /**
     * Looks up a course by id and fails with 404 when it is missing.
     */
    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Curso " + id + " não encontrado"));
    }
}
