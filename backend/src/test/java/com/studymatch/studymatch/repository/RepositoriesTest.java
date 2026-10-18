package com.studymatch.studymatch.repository;

import com.studymatch.studymatch.model.Admin;
import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.CurricularUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoriesTest {

    @Autowired private AdminRepository adminRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CurricularUnitRepository unitRepository;
    @Autowired private TestEntityManager em;

    @Test
    @DisplayName("findByEmailAdmin finds an existing admin and returns null otherwise")
    void findAdminByEmail() {
        adminRepository.save(new Admin(null, "Maria", "maria@studymatch.pt", "hash"));

        assertNotNull(adminRepository.findByEmailAdmin("maria@studymatch.pt"));
        assertNull(adminRepository.findByEmailAdmin("other@studymatch.pt"));
    }

    @Test
    @DisplayName("Two admins cannot share the same email")
    void adminEmailIsUnique() {
        adminRepository.saveAndFlush(new Admin(null, "A", "same@studymatch.pt", "h"));

        assertThrows(DataIntegrityViolationException.class,
                () -> adminRepository.saveAndFlush(new Admin(null, "B", "same@studymatch.pt", "h")));
    }

    @Test
    @DisplayName("findByCodeCourse finds the course by exact code")
    void findCourseByCode() {
        courseRepository.save(new Course(null, "LEI", "Computer Engineering"));

        assertNotNull(courseRepository.findByCodeCourse("LEI"));
        assertNull(courseRepository.findByCodeCourse("lei2"));
    }

    @Test
    @DisplayName("Course name search ignores case and matches substrings")
    void searchCourseByName() {
        courseRepository.save(new Course(null, "LEI", "Computer Engineering"));
        courseRepository.save(new Course(null, "LGE", "Management"));

        List<Course> result = courseRepository.findByNameCourseContainingIgnoreCase("ENGINEER");

        assertEquals(1, result.size());
        assertEquals("LEI", result.get(0).getCodeCourse());
        assertTrue(courseRepository.findByNameCourseContainingIgnoreCase("xyz").isEmpty());
    }

    @Test
    @DisplayName("Two courses cannot share the same code")
    void courseCodeIsUnique() {
        courseRepository.saveAndFlush(new Course(null, "LEI", "One"));

        assertThrows(DataIntegrityViolationException.class,
                () -> courseRepository.saveAndFlush(new Course(null, "LEI", "Two")));
    }

    @Test
    @DisplayName("findByCodeUnit and name search work for curricular units")
    void unitQueries() {
        unitRepository.save(new CurricularUnit(null, "SQ", "Software Quality"));
        unitRepository.save(new CurricularUnit(null, "BD", "Databases"));

        assertNotNull(unitRepository.findByCodeUnit("SQ"));
        assertNull(unitRepository.findByCodeUnit("XX"));
        assertEquals(1, unitRepository.findByNameUnitContainingIgnoreCase("quality").size());
    }

    @Test
    @DisplayName("Course-unit many-to-many relation is persisted and loaded")
    void courseUnitRelationPersists() {
        Course course = new Course(null, "LEI", "Computer Engineering");
        CurricularUnit unit = new CurricularUnit(null, "SQ", "Software Quality");
        unitRepository.save(unit);
        course.addCurricularUnit(unit);
        courseRepository.save(course);
        em.flush();
        em.clear();

        CurricularUnit loadedUnit = unitRepository.findByCodeUnit("SQ");
        assertEquals(1, loadedUnit.getCourses().size());
        assertEquals("LEI", loadedUnit.getCourses().get(0).getCodeCourse());
    }
}
