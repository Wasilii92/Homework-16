package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        student.setId(null);
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student editStudent(Student student) {
        if (!studentRepository.existsById(student.getId())) {
            return null;
        }
        return studentRepository.save(student);
    }

    public Student deleteStudent(long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            studentRepository.deleteById(id);
        }
        return student;
    }

    // Если в контроллерах был метод поиска студентов по имени – оставьте его
    // Но чтобы не менять репозиторий, фильтруем вручную
    public Collection<Student> findStudentsByName(String name) {
        Collection<Student> result = new ArrayList<>();
        for (Student student : studentRepository.findAll()) {
            if (Objects.equals(student.getName(), name)) {
                result.add(student);
            }
        }
        return result;
    }

    // Если был поиск по возрасту – тоже фильтруем здесь
    public Collection<Student> findByAge(int age) {
        Collection<Student> result = new ArrayList<>();
        for (Student student : studentRepository.findAll()) {
            if (student.getAge() == age) {
                result.add(student);
            }
        }
        return result;
    }
}