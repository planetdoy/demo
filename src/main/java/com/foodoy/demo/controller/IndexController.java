package com.foodoy.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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


    /**
     * 파일 경로 확인: 파일 경로가 정확한지 확인하세요. Paths.get(DOWNLOAD_DIR).resolve(fileName).normalize()를 통해 생성된 파일 경로가 실제 파일이 있는 경로인지 확인해야 합니다.
     *
     * 파일 존재 여부 확인: 파일이 실제로 해당 경로에 존재하는지 확인하세요. 만약 파일이 없다면 해당 경로에 파일을 올바르게 저장했는지 다시 확인하세요.
     *
     * 보안 권한 확인: 파일에 접근할 수 있는 권한이 있는지 확인하세요. 애플리케이션이 실행되는 사용자에게 파일에 대한 읽기 권한이 있어야 합니다.
     *
     * URL 형식 확인: UrlResource를 사용할 때 URL 형식이 맞는지 확인하세요. 예를 들어, file://로 시작하는 URL이어야 합니다.
     *
     * 상대 경로 사용: 절대 경로 대신 상대 경로를 사용하여 파일을 로드해 보세요. 예를 들어, Paths.get("downloads").resolve(fileName).normalize()와 같이 상대 경로를 사용할 수 있습니다.
     *
     * 파일 확장자 확인: 파일 확장자가 실제 파일과 일치하는지 확인하세요. 예를 들어, 파일이 .xlsx인데 코드에서는 .xls로 참조하고 있다면 문제가 발생할 수 있습니다.
     * @param fileName
     * @return
     * @throws IOException
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        // Load file as Resource
        String realPath = System.getProperty("user.dir").concat("/download");
        Path filePath = Paths.get(realPath).resolve(fileName).normalize();

        log.debug("file path : {}", filePath);

        Resource resource = new UrlResource(filePath.toUri());

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
