package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private final Faculty testFaculty = new Faculty("Gryffindor", "red");
    private final Student testStudent = new Student("Harry Potter", 17);
    private final Long studentId = 1L;

    // ---------- GET /student/{id} ----------
    @Test
    void getStudentInfo_ShouldReturnStudent_WhenExists() throws Exception {
        testStudent.setId(studentId);
        testStudent.setFaculty(testFaculty);

        when(studentService.findStudent(studentId)).thenReturn(testStudent);

        mockMvc.perform(get("/student/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(17))
                .andExpect(jsonPath("$.faculty.id").value(testFaculty.getId()))
                .andExpect(jsonPath("$.faculty.name").value("Gryffindor"));
    }

    @Test
    void getStudentInfo_ShouldReturnNotFound_WhenStudentDoesNotExist() throws Exception {
        when(studentService.findStudent(anyLong())).thenReturn(null);

        mockMvc.perform(get("/student/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /student ----------
    @Test
    void createStudent_ShouldSaveAndReturnStudent() throws Exception {
        Student newStudent = new Student("Hermione Granger", 16);
        newStudent.setId(2L);
        newStudent.setFaculty(testFaculty);

        when(studentService.addStudent(any(Student.class))).thenReturn(newStudent);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Hermione Granger"))
                .andExpect(jsonPath("$.age").value(16));
    }

    // ---------- PUT /student ----------
    @Test
    void editStudent_ShouldUpdateAndReturnStudent_WhenExists() throws Exception {
        Student updatedStudent = new Student("Harry Potter", 18);
        updatedStudent.setId(studentId);
        updatedStudent.setFaculty(testFaculty);

        when(studentService.editStudent(any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void editStudent_ShouldReturnBadRequest_WhenStudentNotFound() throws Exception {
        Student notExisting = new Student("Unknown", 20);
        notExisting.setId(999L);
        notExisting.setFaculty(testFaculty);

        when(studentService.editStudent(any(Student.class))).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notExisting)))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /student/{id} ----------
    @Test
    void deleteStudent_ShouldReturnOk_Always() throws Exception {
        doNothing().when(studentService).deleteStudent(anyLong());

        mockMvc.perform(delete("/student/{id}", studentId))
                .andExpect(status().isOk());
    }

    // ---------- GET /student (with parameters) ----------
    @Test
    void findStudents_ByAge_ShouldReturnStudentsWithExactAge() throws Exception {
        Student another = new Student("Ron Weasley", 17);
        another.setId(3L);
        List<Student> students = Arrays.asList(testStudent, another);

        when(studentService.findByAge(17)).thenReturn(students);

        mockMvc.perform(get("/student?age=17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].age", everyItem(is(17))));
    }

    @Test
    void findStudents_ByAgeRange_ShouldReturnStudentsBetweenMinAndMax() throws Exception {
        List<Student> students = Collections.singletonList(testStudent);

        when(studentService.findByAge(15, 18)).thenReturn(students);

        mockMvc.perform(get("/student?min=15&max=18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }

    @Test
    void findStudents_NoParams_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /student/{id}/faculty ----------
    @Test
    void getStudentFaculty_ShouldReturnFaculty_WhenStudentExists() throws Exception {
        testStudent.setId(studentId);
        testStudent.setFaculty(testFaculty);

        when(studentService.findStudent(studentId)).thenReturn(testStudent);

        mockMvc.perform(get("/student/{id}/faculty", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFaculty.getId()))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    void getStudentFaculty_ShouldReturnNotFound_WhenStudentDoesNotExist() throws Exception {
        when(studentService.findStudent(anyLong())).thenReturn(null);

        mockMvc.perform(get("/student/{id}/faculty", 999L))
                .andExpect(status().isNotFound());
    }
}