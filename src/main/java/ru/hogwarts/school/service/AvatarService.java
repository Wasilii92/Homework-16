package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;

@Service
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    // Загрузка аватара для студента
    public Avatar uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        // Создаём новый аватар (или обновляем существующий)
        Avatar avatar = avatarRepository.findByStudentId(studentId)
                .orElse(new Avatar());

        avatar.setFilePath("avatars/" + studentId + "_" + file.getOriginalFilename());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(file.getBytes());
        avatar.setStudent(student);

        // Сохраняем аватар
        Avatar savedAvatar = avatarRepository.save(avatar);

        // Обновляем ссылку у студента (чтобы связь была двусторонней)
        student.setAvatar(savedAvatar);
        studentRepository.save(student);

        return savedAvatar;
    }

    // Получение аватара по ID студента
    public Avatar getAvatarByStudentId(Long studentId) {
        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Аватар не найден"));
    }

    // Получение аватара по ID аватара
    public Avatar getAvatarById(Long id) {
        return avatarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аватар не найден"));
    }
}