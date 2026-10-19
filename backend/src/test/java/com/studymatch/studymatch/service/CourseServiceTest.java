package com.studymatch.studymatch.service;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.CurricularUnit;
import com.studymatch.studymatch.repository.CourseRepository;
import com.studymatch.studymatch.repository.CurricularUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    private CourseRepository courseRepository;
    private CurricularUnitRepository unitRepository;
    private CourseService service;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepository.class);
        unitRepository = Mockito.mock(CurricularUnitRepository.class);
        service = new CourseService(courseRepository, unitRepository);
    }

    private Course course(Long id, String code, String name) {
        return new Course(id, code, name);
    }

    private CurricularUnit unit(Long id, String code) {
        return new CurricularUnit(id, code, "Unit " + code);
    }

    // ---- getAllCourses / getCourseById ----

    @Test
    @DisplayName("getAllCourses returns what the repository returns")
    void getAllCourses() {
        List<Course> courses = List.of(course(1L, "LEI", "A"), course(2L, "LGE", "B"));
        when(courseRepository.findAll()).thenReturn(courses);

        assertSame(courses, service.getAllCourses());
    }

    @Test
    @DisplayName("getCourseById returns the course when it exists")
    void getCourseByIdFound() {
        Course c = course(1L, "LEI", "A");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));

        assertSame(c, service.getCourseById(1L));
    }

    @Test
    @DisplayName("getCourseById throws 404 when the course does not exist")
    void getCourseByIdNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getCourseById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("99"));
    }

    // ---- createCourse ----

    @Test
    @DisplayName("createCourse saves a new course and ignores any id sent by the client")
    void createCourseSavesWithoutClientId() {
        when(courseRepository.findByCodeCourse("LEI")).thenReturn(null);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createCourse(course(55L, "LEI", "Computer Engineering"));

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertNull(captor.getValue().getIdCourse());
        assertEquals("LEI", captor.getValue().getCodeCourse());
        assertEquals("Computer Engineering", captor.getValue().getNameCourse());
    }

    @Test
    @DisplayName("createCourse with an existing code throws 409 and does not save")
    void createCourseDuplicateCode() {
        when(courseRepository.findByCodeCourse("LEI")).thenReturn(course(1L, "LEI", "Existing"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createCourse(course(null, "LEI", "New")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("LEI"));
        verify(courseRepository, never()).save(any());
    }

    // ---- updateCourse ----

    @Test
    @DisplayName("updateCourse throws 404 when the course does not exist")
    void updateCourseNotFound() {
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateCourse(9L, course(null, "NEW", "New")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(courseRepository, never()).findByCodeCourse(any());
    }

    @Test
    @DisplayName("updateCourse with a code used by ANOTHER course throws 409 and changes nothing")
    void updateCourseCodeUsedByOther() {
        Course target = course(1L, "OLD", "Old name");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(target));
        when(courseRepository.findByCodeCourse("TAKEN")).thenReturn(course(2L, "TAKEN", "Other"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateCourse(1L, course(null, "TAKEN", "New name")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("OLD", target.getCodeCourse());
        assertEquals("Old name", target.getNameCourse());
    }

    @Test
    @DisplayName("updateCourse may keep the course's own code and change only the name")
    void updateCourseKeepsOwnCode() {
        Course target = course(1L, "LEI", "Old name");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(target));
        when(courseRepository.findByCodeCourse("LEI")).thenReturn(target);

        Course result = service.updateCourse(1L, course(null, "LEI", "New name"));

        assertSame(target, result);
        assertEquals("LEI", result.getCodeCourse());
        assertEquals("New name", result.getNameCourse());
    }

    @Test
    @DisplayName("updateCourse with a free code changes code and name")
    void updateCourseWithFreeCode() {
        Course target = course(1L, "OLD", "Old name");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(target));
        when(courseRepository.findByCodeCourse("NEW")).thenReturn(null);

        Course result = service.updateCourse(1L, course(null, "NEW", "New name"));

        assertEquals("NEW", result.getCodeCourse());
        assertEquals("New name", result.getNameCourse());
    }

    // ---- setCourseUnits ----

    @Test
    @DisplayName("setCourseUnits throws 404 when the course does not exist")
    void setUnitsCourseNotFound() {
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.setCourseUnits(9L, List.of(1L)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(unitRepository);
    }

    @Test
    @DisplayName("setCourseUnits throws 404 when some unit id does not exist and leaves the course untouched")
    void setUnitsUnknownUnit() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        c.addCurricularUnit(u1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of(unit(2L, "U2"))); // 3 was not found

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.setCourseUnits(1L, List.of(2L, 3L)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(List.of(u1), c.getCurricularUnits());
    }

    @Test
    @DisplayName("setCourseUnits adds the wanted units on both sides of the relation")
    void setUnitsAddsNewUnits() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        CurricularUnit u2 = unit(2L, "U2");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of(u1, u2));

        Course result = service.setCourseUnits(1L, List.of(1L, 2L));

        assertEquals(2, result.getCurricularUnits().size());
        assertTrue(u1.getCourses().contains(c));
        assertTrue(u2.getCourses().contains(c));
    }

    @Test
    @DisplayName("setCourseUnits removes the units that are no longer wanted")
    void setUnitsRemovesUnwanted() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        CurricularUnit u2 = unit(2L, "U2");
        c.addCurricularUnit(u1);
        c.addCurricularUnit(u2);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of(u2));

        Course result = service.setCourseUnits(1L, List.of(2L));

        assertEquals(List.of(u2), result.getCurricularUnits());
        assertFalse(u1.getCourses().contains(c));
        assertTrue(u2.getCourses().contains(c));
    }

    @Test
    @DisplayName("setCourseUnits keeps existing units (no duplicates) and adds only the missing ones")
    void setUnitsKeepsExistingAndAddsMissing() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        CurricularUnit u3 = unit(3L, "U3");
        c.addCurricularUnit(u1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of(u1, u3));

        Course result = service.setCourseUnits(1L, List.of(1L, 3L));

        assertEquals(2, result.getCurricularUnits().size());
        assertTrue(result.getCurricularUnits().contains(u1));
        assertTrue(result.getCurricularUnits().contains(u3));
        assertEquals(1, u1.getCourses().size());
    }

    @Test
    @DisplayName("setCourseUnits with an empty list removes every unit")
    void setUnitsEmptyListRemovesAll() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        c.addCurricularUnit(u1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of());

        Course result = service.setCourseUnits(1L, List.of());

        assertTrue(result.getCurricularUnits().isEmpty());
        assertTrue(u1.getCourses().isEmpty());
    }

    @Test
    @DisplayName("setCourseUnits treats repeated ids as one unit")
    void setUnitsDuplicatedIds() {
        Course c = course(1L, "LEI", "A");
        CurricularUnit u1 = unit(1L, "U1");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));
        when(unitRepository.findAllById(any())).thenReturn(List.of(u1));

        Course result = service.setCourseUnits(1L, List.of(1L, 1L));

        assertEquals(1, result.getCurricularUnits().size());
    }
}
