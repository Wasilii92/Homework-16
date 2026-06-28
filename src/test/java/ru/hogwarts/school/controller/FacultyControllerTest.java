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
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    private String baseUrl;
    private Faculty testFaculty;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/faculty";

        // Создаём тестовый факультет
        testFaculty = new Faculty("Gryffindor", "red");
        testFaculty = facultyRepository.save(testFaculty);

        // Создаём тестового студента, связанного с факультетом
        testStudent = new Student("Harry Potter", 17);
        testStudent.setFaculty(testFaculty);
        testStudent = studentRepository.save(testStudent);
    }

    // ---------- GET /faculty/{id} ----------
    @Test
    void getFacultyInfo_ShouldReturnFaculty_WhenExists() {
        Long facultyId = testFaculty.getId();

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                Faculty.class,
                facultyId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(facultyId, response.getBody().getId());
        assertEquals("Gryffindor", response.getBody().getName());
        assertEquals("red", response.getBody().getColor());
    }

    @Test
    void getFacultyInfo_ShouldReturnNotFound_WhenNotExists() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                Faculty.class,
                999L
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------- POST /faculty ----------
    @Test
    void createFaculty_ShouldSaveAndReturnFaculty() {
        Faculty newFaculty = new Faculty("Slytherin", "green");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                baseUrl,
                newFaculty,
                Faculty.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Slytherin", response.getBody().getName());
        assertEquals("green", response.getBody().getColor());

        assertTrue(facultyRepository.findById(response.getBody().getId()).isPresent());
    }

    // ---------- PUT /faculty ----------
    @Test
    void editFaculty_ShouldUpdateAndReturnFaculty_WhenExists() {
        Faculty updated = new Faculty("Gryffindor", "gold");
        updated.setId(testFaculty.getId());

        HttpEntity<Faculty> request = new HttpEntity<>(updated);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Faculty.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("gold", response.getBody().getColor());

        Faculty saved = facultyRepository.findById(testFaculty.getId()).orElseThrow();
        assertEquals("gold", saved.getColor());
    }

    @Test
    void editFaculty_ShouldReturnBadRequest_WhenFacultyNotFound() {
        Faculty notExisting = new Faculty("Unknown", "black");
        notExisting.setId(999L);

        HttpEntity<Faculty> request = new HttpEntity<>(notExisting);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Faculty.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---------- DELETE /faculty/{id} ----------
    @Test
    void deleteFaculty_ShouldReturnOk_WhenExists() {
        Long facultyId = testFaculty.getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                facultyId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(facultyRepository.existsById(facultyId));
    }

    @Test
    void deleteFaculty_ShouldReturnOk_EvenWhenNotExists() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                999L
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ---------- GET /faculty?color=... ----------
    @Test
    void findFaculties_ByColor_ShouldReturnFacultiesWithExactColor() {
        // Создаём ещё один факультет того же цвета
        Faculty another = new Faculty("Hufflepuff", "yellow");
        facultyRepository.save(another);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                baseUrl + "?color=yellow",
                Faculty[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Faculty> faculties = List.of(response.getBody());
        assertEquals(1, faculties.size());
        assertEquals("Hufflepuff", faculties.get(0).getName());
    }

    // ---------- GET /faculty?query=... (поиск по имени или цвету) ----------
    @Test
    void findFaculties_ByQuery_ShouldReturnFacultiesMatchingNameOrColor() {
        // Создаём факультет с именем, содержащим "Raven"
        Faculty raven = new Faculty("Ravenclaw", "blue");
        facultyRepository.save(raven);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                baseUrl + "?query=raven",
                Faculty[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Faculty> faculties = List.of(response.getBody());
        assertTrue(faculties.stream().anyMatch(f -> f.getName().equalsIgnoreCase("Ravenclaw")));
    }

    // ---------- GET /faculty/search?query=... ----------
    @Test
    void searchFaculties_ShouldReturnFacultiesMatchingNameOrColor() {
        Faculty raven = new Faculty("Ravenclaw", "blue");
        facultyRepository.save(raven);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                baseUrl + "/search?query=blue",
                Faculty[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Faculty> faculties = List.of(response.getBody());
        assertTrue(faculties.stream().anyMatch(f -> f.getColor().equalsIgnoreCase("blue")));
    }

    @Test
    void searchFaculties_ShouldReturnBadRequest_WhenQueryIsBlank() {
        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                baseUrl + "/search?query=",
                Faculty[].class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---------- GET /faculty/{id}/students ----------
    @Test
    void getFacultyStudents_ShouldReturnStudents_WhenFacultyExists() {
        Long facultyId = testFaculty.getId();

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "/{id}/students",
                Student[].class,
                facultyId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Student> students = List.of(response.getBody());
        assertEquals(1, students.size());
        assertEquals(testStudent.getId(), students.get(0).getId());
        assertEquals("Harry Potter", students.get(0).getName());
    }

    @Test
    void getFacultyStudents_ShouldReturnEmptyList_WhenFacultyHasNoStudents() {
        // Создаём факультет без студентов
        Faculty emptyFaculty = new Faculty("Empty", "none");
        emptyFaculty = facultyRepository.save(emptyFaculty);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "/{id}/students",
                Student[].class,
                emptyFaculty.getId()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }
}