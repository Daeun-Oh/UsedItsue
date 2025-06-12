package org.koreait.product.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ProductFileUploadService {

    @Value("${file.path}")
    private String uploadPath;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));
        System.out.println("설정된 업로드 경로: " + uploadPath);

        String projectRoot = System.getProperty("user.dir");
        String uploadPath = projectRoot + "/src/main/resources/static/uploads/products/";


        // 업로드 디렉토리 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        String filePath = uploadDir.getAbsolutePath() + File.separator + storedFilename;
        file.transferTo(new File(filePath));

        // 웹에서 접근 가능한 경로 반환
        return "/uploads/products/"+storedFilename;
    }

    public void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            File file = new File(uploadPath + filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}