package ru.kharevich.imageservice.controller.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.FileResponse;
import ru.kharevich.imageservice.service.impl.S3StorageService;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;


/*
   Контроллер создан исключительно для тестирования и не используется в проекте
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3StorageService s3StorageService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FileResponse uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filename", required = false) String customFilename) {

        String filename = s3StorageService.uploadFile(file, customFilename);
        String fileUrl = s3StorageService.getFileUrl(filename);

        FileResponse response = new FileResponse(
                filename,
                fileUrl,
                file.getSize(),
                file.getContentType()
        );
        return response;
    }

    @GetMapping("/download/{filename}")
    @ResponseStatus(HttpStatus.OK)
    public byte[] downloadFile(@PathVariable String filename, HttpServletResponse response) {
        byte[] fileContent = s3StorageService.downloadFile(filename);
        HeadObjectResponse fileInfo = s3StorageService.getFileInfo(filename);

        response.setContentType(fileInfo.contentType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        response.setContentLength(fileContent.length);
        return fileContent;
    }

    @GetMapping("/{filename}/url")
    public ResponseEntity<String> getFileUrl(@PathVariable String filename) {
        try {
            String fileUrl = s3StorageService.getFileUrl(filename);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{filename}")
    public void deleteFile(@PathVariable String filename) {
        s3StorageService.deleteFile(filename);
    }

}