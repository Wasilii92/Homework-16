package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;

@RestController
@RequestMapping("/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    @Autowired
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    // Загрузка аватара для конкретного студента
    @PostMapping(value = "/students/{studentId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Avatar> uploadAvatar(@PathVariable Long studentId,
                                               @RequestParam("avatar") MultipartFile file) {
        try {
            Avatar avatar = avatarService.uploadAvatar(studentId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(avatar);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Получение аватара студента в виде изображения
    @GetMapping("/students/{studentId}/avatar")
    public ResponseEntity<byte[]> getAvatarByStudentId(@PathVariable Long studentId) {
        try {
            Avatar avatar = avatarService.getAvatarByStudentId(studentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
            headers.setContentLength(avatar.getFileSize());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(avatar.getData());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // (Опционально) Получение информации об аватаре по ID студента (JSON)
    @GetMapping("/students/{studentId}/avatar/info")
    public ResponseEntity<Avatar> getAvatarInfo(@PathVariable Long studentId) {
        try {
            Avatar avatar = avatarService.getAvatarByStudentId(studentId);
            return ResponseEntity.ok(avatar);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}