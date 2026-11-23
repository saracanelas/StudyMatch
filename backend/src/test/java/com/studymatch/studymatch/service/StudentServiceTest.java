package com.studymatch.studymatch.service;

import com.studymatch.studymatch.dto.StudentRequest;
import com.studymatch.studymatch.model.Course;
import com.studymatch.studymatch.model.Student;
import com.studymatch.studymatch.repository.CourseRepository;
import com.studymatch.studymatch.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private StudentService service;

    @BeforeEach
    void setUp() {
        studentRepository = Mockito.mock(StudentRepository.class);
        courseRepository = Mockito.mock(CourseRepository.class);
        service = new StudentService(studentRepository, courseRepository);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static StudentRequest request(String number, String email) {
        return new StudentRequest(number, "Student " + number, email, 2024);
    }

    private static Student student(Long id, String number, String email) {
        return new Student(id, number, "Student " + number, email, 2024);
    }

    @Test
    @DisplayName("getAllStudents returns what the repository returns")
    void getAll() {
        List<Student> students = List.of(student(1L, "1", "a@x.pt"));
        when(studentRepository.findAll()).thenReturn(students);

        assertSame(students, service.getAllStudents());
    }

    @Test
    @DisplayName("getStudentById returns the student, or 404 when it does not exist")
    void getById() {
        Student s = student(1L, "1", "a@x.pt");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertSame(s, service.getStudentById(1L));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getStudentById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("createStudent trims the data, lowercases the e-mail, links the course and starts without id and average")
    void createStudent() {
        Course lei = new Course(3L, "LEI", "Computer Engineering");
        when(courseRepository.findById(3L)).thenReturn(Optional.of(lei));
        StudentRequest sent = new StudentRequest(" 2024001 ", " Ana Costa ", " Ana@StudyMatch.pt ",
                LocalDate.of(2004, 5, 1), 2024, new StudentRequest.CourseReference(3L));

        Student created = service.createStudent(sent);

        assertNull(created.getIdStudent());
        assertEquals("2024001", created.getNumberStudent());
        assertEquals("Ana Costa", created.getNameStudent());
        assertEquals("ana@studymatch.pt", created.getEmailStudent());
        assertEquals(LocalDate.of(2004, 5, 1), created.getBirthday());
        assertSame(lei, created.getCourse());
        assertNull(created.getGradeAverage());
    }

    @Test
    @DisplayName("createStudent without course leaves the course empty")
    void createWithoutCourse() {
        Student created = service.createStudent(request("1", "a@x.pt"));

        assertNull(created.getCourse());
        verifyNoInteractions(courseRepository);
    }

    @Test
    @DisplayName("createStudent refuses a number or e-mail already in use (409)")
    void createDuplicates() {
        when(studentRepository.findByNumberStudent("1")).thenReturn(student(5L, "1", "other@x.pt"));
        ResponseStatusException byNumber = assertThrows(ResponseStatusException.class,
                () -> service.createStudent(request("1", "a@x.pt")));
        assertEquals(HttpStatus.CONFLICT, byNumber.getStatusCode());

        when(studentRepository.findByEmailStudent("a@x.pt")).thenReturn(student(5L, "9", "a@x.pt"));
        ResponseStatusException byEmail = assertThrows(ResponseStatusException.class,
                () -> service.createStudent(request("2", "a@x.pt")));
        assertEquals(HttpStatus.CONFLICT, byEmail.getStatusCode());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStudent with a course that does not exist fails with 404")
    void createWithMissingCourse() {
        when(courseRepository.findById(9L)).thenReturn(Optional.empty());
        StudentRequest sent = new StudentRequest("1", "Student 1", "a@x.pt", null, 2024,
                new StudentRequest.CourseReference(9L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createStudent(sent));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStudent changes the data, keeps its own number/e-mail and keeps the average")
    void updateStudent() {
        Student stored = student(1L, "1", "a@x.pt");
        stored.setGradeAverage(13.0);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(studentRepository.findByNumberStudent("1")).thenReturn(stored);
        when(studentRepository.findByEmailStudent("a@x.pt")).thenReturn(stored);
        StudentRequest changes = new StudentRequest("1", "New name", "a@x.pt", null, 2022, null);

        Student result = service.updateStudent(1L, changes);

        assertSame(stored, result);
        assertEquals("New name", result.getNameStudent());
        assertEquals(2022, result.getEnrollmentYear());
        assertEquals(13.0, result.getGradeAverage());
    }

    @Test
    @DisplayName("updateStudent refuses a number used by another student and 404s for a missing student")
    void updateErrors() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "1", "a@x.pt")));
        when(studentRepository.findByNumberStudent("2")).thenReturn(student(2L, "2", "b@x.pt"));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.updateStudent(1L, request("2", "a@x.pt"))).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.updateStudent(99L, request("3", "c@x.pt"))).getStatusCode());
    }
}
