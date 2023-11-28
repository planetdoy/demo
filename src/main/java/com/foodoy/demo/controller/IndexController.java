package com.foodoy.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Controller
public class IndexController {
    private static final String UPLOAD_DIR = "\\upload";
    private static final String DOWNLOAD_DIR = "\\download";

    @GetMapping("/")
    public String hello(Model model) {
        model.addAttribute("message", "Hello, Foodoy Service!");
        return "index";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        // Load file as Resource
        Path filePath = Paths.get(DOWNLOAD_DIR).resolve(fileName).normalize();
        Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        // Content-Disposition header
        String contentDisposition = "attachment; filename=" + resource.getFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            // 파일이 비어있는 경우
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        try {
            // 업로드 디렉토리에 파일 저장
            Path uploadPath = Path.of(UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            redirectAttributes.addFlashAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "File upload failed: " + e.getMessage());
        }

        return "redirect:/";
    }
}
