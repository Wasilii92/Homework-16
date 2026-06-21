package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByFacultyId(Long facultyId);
    List<Student> findByAgeBetween(int min, int max);   // ← добавить эту строку
}
