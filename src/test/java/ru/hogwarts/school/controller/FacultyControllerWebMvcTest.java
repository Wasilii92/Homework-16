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
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FacultyService facultyService;

    @MockBean
    private StudentService studentService;  // используется в эндпоинте /{id}/students

    private final Faculty testFaculty = new Faculty("Gryffindor", "red");
    private final Long facultyId = 1L;
    private final Student testStudent = new Student("Harry Potter", 17);

    // ---------- GET /faculty/{id} ----------
    @Test
    void getFacultyInfo_ShouldReturnFaculty_WhenExists() throws Exception {
        testFaculty.setId(facultyId);

        when(facultyService.findFaculty(facultyId)).thenReturn(testFaculty);

        mockMvc.perform(get("/faculty/{id}", facultyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyId))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("red"));
    }

    @Test
    void getFacultyInfo_ShouldReturnNotFound_WhenFacultyDoesNotExist() throws Exception {
        when(facultyService.findFaculty(anyLong())).thenReturn(null);

        mockMvc.perform(get("/faculty/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /faculty ----------
    @Test
    void createFaculty_ShouldSaveAndReturnFaculty() throws Exception {
        Faculty newFaculty = new Faculty("Slytherin", "green");
        newFaculty.setId(2L);

        when(facultyService.addFaculty(any(Faculty.class))).thenReturn(newFaculty);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Slytherin"))
                .andExpect(jsonPath("$.color").value("green"));
    }

    // ---------- PUT /faculty ----------
    @Test
    void editFaculty_ShouldUpdateAndReturnFaculty_WhenExists() throws Exception {
        Faculty updatedFaculty = new Faculty("Gryffindor", "gold");
        updatedFaculty.setId(facultyId);

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyId))
                .andExpect(jsonPath("$.color").value("gold"));
    }

    @Test
    void editFaculty_ShouldReturnBadRequest_WhenFacultyNotFound() throws Exception {
        Faculty notExisting = new Faculty("Unknown", "black");
        notExisting.setId(999L);

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(null);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notExisting)))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /faculty/{id} ----------
    @Test
    void deleteFaculty_ShouldReturnOk_Always() throws Exception {
        doNothing().when(facultyService).deleteFaculty(anyLong());

        mockMvc.perform(delete("/faculty/{id}", facultyId))
                .andExpect(status().isOk());
    }

    // ---------- GET /faculty/search?query=... ----------
    @Test
    void searchFaculties_WithQuery_ShouldReturnFaculties() throws Exception {
        List<Faculty> faculties = Collections.singletonList(testFaculty);

        when(facultyService.findByNameOrColorIgnoreCase("Gryffindor")).thenReturn(faculties);

        mockMvc.perform(get("/faculty/search?query=Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void searchFaculties_WithBlankQuery_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/faculty/search?query="))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /faculty (without path) ----------
    @Test
    void findFaculties_ByColor_ShouldReturnFaculties() throws Exception {
        List<Faculty> faculties = Collections.singletonList(testFaculty);

        when(facultyService.findByColor("red")).thenReturn(faculties);

        mockMvc.perform(get("/faculty?color=red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void findFaculties_ByQuery_ShouldReturnFaculties() throws Exception {
        List<Faculty> faculties = Collections.singletonList(testFaculty);

        when(facultyService.findByNameOrColorIgnoreCase("Gryffindor")).thenReturn(faculties);

        mockMvc.perform(get("/faculty?query=Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"));
    }

    @Test
    void findFaculties_NoParams_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /faculty/{id}/students ----------
    @Test
    void getFacultyStudents_ShouldReturnStudents() throws Exception {
        testStudent.setId(1L);
        List<Student> students = Collections.singletonList(testStudent);

        when(studentService.getStudentsByFacultyId(facultyId)).thenReturn(students);

        mockMvc.perform(get("/faculty/{id}/students", facultyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }

    @Test
    void getFacultyStudents_ShouldReturnEmptyList_WhenNoStudents() throws Exception {
        when(studentService.getStudentsByFacultyId(facultyId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/{id}/students", facultyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}