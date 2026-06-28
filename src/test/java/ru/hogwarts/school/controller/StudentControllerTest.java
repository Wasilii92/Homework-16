package ru.hogwarts.school.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    private String baseUrl;
    private Faculty testFaculty;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student";

        testFaculty = new Faculty("Gryffindor", "red");
        testFaculty = facultyRepository.save(testFaculty);

        testStudent = new Student("Harry Potter", 17);
        testStudent.setFaculty(testFaculty);
        testStudent = studentRepository.save(testStudent);
    }

    @Test
    void getStudentInfo_ShouldReturnStudent_WhenExists() {
        Long studentId = testStudent.getId();

        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                Student.class,
                studentId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(studentId, response.getBody().getId());
        assertEquals("Harry Potter", response.getBody().getName());
    }

    @Test
    void getStudentInfo_ShouldReturnNotFound_WhenNotExists() {
        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                Student.class,
                999L
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createStudent_ShouldSaveAndReturnStudent() {
        Student newStudent = new Student("Hermione Granger", 16);
        newStudent.setFaculty(testFaculty);

        ResponseEntity<Student> response = restTemplate.postForEntity(
                baseUrl,
                newStudent,
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Hermione Granger", response.getBody().getName());
        assertEquals(16, response.getBody().getAge());

        assertTrue(studentRepository.findById(response.getBody().getId()).isPresent());
    }

    @Test
    void editStudent_ShouldUpdateAndReturnStudent_WhenExists() {
        Student updated = new Student("Harry Potter", 18);
        updated.setId(testStudent.getId());
        updated.setFaculty(testFaculty);

        HttpEntity<Student> request = new HttpEntity<>(updated);

        ResponseEntity<Student> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(18, response.getBody().getAge());

        Student saved = studentRepository.findById(testStudent.getId()).orElseThrow();
        assertEquals(18, saved.getAge());
    }

    @Test
    void editStudent_ShouldReturnBadRequest_WhenStudentNotFound() {
        Student notExisting = new Student("Unknown", 20);
        notExisting.setId(999L);
        notExisting.setFaculty(testFaculty);

        HttpEntity<Student> request = new HttpEntity<>(notExisting);

        ResponseEntity<Student> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Student.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteStudent_ShouldReturnOk_WhenExists() {
        Long studentId = testStudent.getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                studentId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(studentRepository.existsById(studentId));
    }

    @Test
    void deleteStudent_ShouldReturnOk_EvenWhenNotExists() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                999L
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void findStudents_ByAge_ShouldReturnStudentsWithExactAge() {
        Student another = new Student("Ron Weasley", 17);
        another.setFaculty(testFaculty);
        studentRepository.save(another);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "?age=17",
                Student[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Student> students = List.of(response.getBody());
        assertTrue(students.size() >= 2);
        assertTrue(students.stream().allMatch(s -> s.getAge() == 17));
    }

    @Test
    void findStudents_ByAgeRange_ShouldReturnStudentsBetweenMinAndMax() {
        Student young = new Student("Neville", 14);
        young.setFaculty(testFaculty);
        studentRepository.save(young);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "?min=15&max=18",
                Student[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Student> students = List.of(response.getBody());
        assertTrue(students.stream().allMatch(s -> s.getAge() >= 15 && s.getAge() <= 18));
        assertTrue(students.stream().anyMatch(s -> s.getId().equals(testStudent.getId())));
        assertFalse(students.stream().anyMatch(s -> s.getName().equals("Neville")));
    }

    @Test
    void findStudents_NoParams_ShouldReturnEmptyList() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl,
                Student[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void getStudentFaculty_ShouldReturnFaculty_WhenStudentExists() {
        Long studentId = testStudent.getId();

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/{id}/faculty",
                Faculty.class,
                studentId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testFaculty.getId(), response.getBody().getId());
        assertEquals("Gryffindor", response.getBody().getName());
    }

    @Test
    void getStudentFaculty_ShouldReturnNotFound_WhenStudentNotExists() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/{id}/faculty",
                Faculty.class,
                999L
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}