package org.koreait.product.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class ProductFileUploadService {

    private final FileProperties fileProperties;
    private final HttpServletRequest request;
//
//    @Value("${file.path}")
//    private String uploadPath;

    public String getUploadImagePath(MultipartFile file, String fileName)  {
        if (file == null) {
            return null;
        }

        String outputDir = fileProperties.getPath() + "/products";

        System.out.println("설정된 업로드 경로: " + outputDir);

        // 업로드 디렉토리 생성
        File uploadDir = new File(outputDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

//        // 파일명 생성 (UUID + 원본 파일명)
//        String originalFilename = file.getOriginalFilename();
//        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        String storedFilename = UUID.randomUUID().toString() + extension;
//
//        // 파일 저장
//        String filePath = uploadDir.getAbsolutePath() + File.separator + storedFilename;
        try {
            file.transferTo(new File(outputDir + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 웹에서 접근 가능한 경로 반환
        return String.format("%s/%s/products/%s", request.getContextPath().replace("/", ""), fileProperties.getUrl(), fileName);
    }

    public void deleteImage(String imagePath) {
        String outputDir = fileProperties.getPath() + "/products";

        if (imagePath != null && !imagePath.isEmpty()) {
            String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            File file = new File(outputDir + filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}