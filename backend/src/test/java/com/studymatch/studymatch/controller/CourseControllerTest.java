package com.studymatch.studymatch.controller;

import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CourseControllerTest {

    private CourseService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseService = Mockito.mock(CourseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build();
    }

    private static String body(String code, String name) {
        return "{\"codeCourse\":\"" + code + "\",\"nameCourse\":\"" + name + "\"}";
    }

    private static ResponseStatusException error(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }

    // ---- GET /api/courses ----

    @Test
    @DisplayName("GET /api/courses returns the list of courses")
    void getAllCourses() throws Exception {
        when(courseService.getAllCourses()).thenReturn(List.of(
                new Course(1L, "LEI", "Computer Engineering"),
                new Course(2L, "LGE", "Management")));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codeCourse").value("LEI"))
                .andExpect(jsonPath("$[1].nameCourse").value("Management"));
    }

    // ---- GET /api/courses/{id} ----

    @Test
    @DisplayName("GET /api/courses/{id} returns the course")
    void getCourseById() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(new Course(1L, "LEI", "Computer Engineering"));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCourse").value(1))
                .andExpect(jsonPath("$.codeCourse").value("LEI"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns 404 with { error } when the course does not exist")
    void getCourseByIdNotFound() throws Exception {
        when(courseService.getCourseById(99L)).thenThrow(error(HttpStatus.NOT_FOUND, "Course 99 not found"));

        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Course 99 not found"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} with a non-numeric id returns 400")
    void getCourseByIdInvalidId() throws Exception {
        mockMvc.perform(get("/api/courses/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    // ---- POST /api/courses ----

    @Test
    @DisplayName("POST /api/courses returns 201 with the created course")
    void createCourse() throws Exception {
        when(courseService.createCourse(any(Course.class))).thenReturn(new Course(1L, "LEI", "Computer Engineering"));

        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(body("LEI", "Computer Engineering")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCourse").value(1))
                .andExpect(jsonPath("$.codeCourse").value("LEI"));
    }

    @Test
    @DisplayName("POST /api/courses returns 409 with { error } when the code is taken")
    void createCourseConflict() throws Exception {
        when(courseService.createCourse(any(Course.class))).thenThrow(error(HttpStatus.CONFLICT, "Code already used"));

        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(body("LEI", "Computer Engineering")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Code already used"));
    }

    @Test
    @DisplayName("POST /api/courses with a blank code returns 400 without calling the service")
    void createCourseBlankCode() throws Exception {
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON).content(body("", "Name")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    @Test
    @DisplayName("POST /api/courses with a blank name returns 400")
    void createCourseBlankName() throws Exception {
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON).content(body("LEI", " ")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    @Test
    @DisplayName("POST /api/courses with a 13-character code returns 400")
    void createCourseCodeTooLong() throws Exception {
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(body("C".repeat(13), "Name")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    @Test
    @DisplayName("POST /api/courses with a 12-character code is accepted")
    void createCourseCodeMaxLength() throws Exception {
        when(courseService.createCourse(any(Course.class))).thenReturn(new Course(1L, "C".repeat(12), "Name"));

        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(body("C".repeat(12), "Name")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/courses with a 51-character name returns 400")
    void createCourseNameTooLong() throws Exception {
        mockMvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON)
                        .content(body("LEI", "N".repeat(51))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    // ---- PUT /api/courses/{id} ----

    @Test
    @DisplayName("PUT /api/courses/{id} returns 200 with the updated course")
    void updateCourse() throws Exception {
        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(new Course(1L, "NEW", "New name"));

        mockMvc.perform(put("/api/courses/1").contentType(MediaType.APPLICATION_JSON).content(body("NEW", "New name")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeCourse").value("NEW"))
                .andExpect(jsonPath("$.nameCourse").value("New name"));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} returns 404 when the course does not exist")
    void updateCourseNotFound() throws Exception {
        when(courseService.updateCourse(eq(9L), any(Course.class))).thenThrow(error(HttpStatus.NOT_FOUND, "Course 9 not found"));

        mockMvc.perform(put("/api/courses/9").contentType(MediaType.APPLICATION_JSON).content(body("NEW", "New name")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Course 9 not found"));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} returns 409 when the code belongs to another course")
    void updateCourseConflict() throws Exception {
        when(courseService.updateCourse(eq(1L), any(Course.class))).thenThrow(error(HttpStatus.CONFLICT, "Code already used"));

        mockMvc.perform(put("/api/courses/1").contentType(MediaType.APPLICATION_JSON).content(body("TAKEN", "Name")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Code already used"));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} with an invalid body returns 400 without calling the service")
    void updateCourseInvalidBody() throws Exception {
        mockMvc.perform(put("/api/courses/1").contentType(MediaType.APPLICATION_JSON).content(body("", "")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courseService);
    }

    // ---- PUT /api/courses/{id}/units ----

    @Test
    @DisplayName("PUT /api/courses/{id}/units passes the list of ids to the service and returns 200")
    void setCourseUnits() throws Exception {
        when(courseService.setCourseUnits(1L, List.of(1L, 2L))).thenReturn(new Course(1L, "LEI", "Computer Engineering"));

        mockMvc.perform(put("/api/courses/1/units").contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeCourse").value("LEI"));

        verify(courseService).setCourseUnits(1L, List.of(1L, 2L));
    }

    @Test
    @DisplayName("PUT /api/courses/{id}/units accepts an empty list")
    void setCourseUnitsEmptyList() throws Exception {
        when(courseService.setCourseUnits(1L, List.of())).thenReturn(new Course(1L, "LEI", "Computer Engineering"));

        mockMvc.perform(put("/api/courses/1/units").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/courses/{id}/units returns 404 when a unit does not exist")
    void setCourseUnitsNotFound() throws Exception {
        when(courseService.setCourseUnits(eq(1L), anyList())).thenThrow(error(HttpStatus.NOT_FOUND, "Unit not found"));

        mockMvc.perform(put("/api/courses/1/units").contentType(MediaType.APPLICATION_JSON).content("[1,99]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Unit not found"));
    }
}
