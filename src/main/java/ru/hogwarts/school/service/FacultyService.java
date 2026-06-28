package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    @Autowired  // явно указываем (можно опустить, но для наглядности оставим)
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty addFaculty(Faculty faculty) {
        // id сгенерирует база (в модели должно быть @GeneratedValue)
        faculty.setId(null);
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(long id) {
        return facultyRepository.findById(id).orElse(null);
    }

    public Faculty editFaculty(Faculty faculty) {
        if (!facultyRepository.existsById(faculty.getId())) {
            return null;
        }
        return facultyRepository.save(faculty);
    }

    public Faculty deleteFaculty(long id) {
        Faculty faculty = facultyRepository.findById(id).orElse(null);
        if (faculty != null) {
            facultyRepository.deleteById(id);
        }
        return faculty;
    }

    // Метод findByColor – как раньше, фильтруем в памяти (репозиторий не трогаем)
    public Collection<Faculty> findByColor(String color) {
        ArrayList<Faculty> result = new ArrayList<>();
        for (Faculty faculty : facultyRepository.findAll()) {
            if (Objects.equals(faculty.getColor(), color)) {
                result.add(faculty);
            }
        }
        return result;
    }
    public Collection<Faculty> findByNameOrColorContainingIgnoreCase(String query) {
        return facultyRepository.findByNameOrColorIgnoreCase(query);
    }
}