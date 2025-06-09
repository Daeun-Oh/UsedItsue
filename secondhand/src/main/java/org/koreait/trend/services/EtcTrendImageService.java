package org.koreait.trend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.koreait.global.configs.PythonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({PythonProperties.class, FileProperties.class})
public class EtcTrendImageService {

    private final PythonProperties properties;  // python 실행 경로 설정
    private final FileProperties fileProperties;// 파일 경로 및 URL 설정
    private final WebApplicationContext ctx;    // Spring 컨텍스트 (환경설정 확인용)
    private final HttpServletRequest request;   // contextPath 추출용
    private final ObjectMapper om;              // JSON 변환용

    /**
     * 병합된 키워드 데이터로 워드클라우드 이미지 생성
     *
     * - generate_image.py 호출
     * - 결과 이미지 파일명을 반환
     */
    public String process(Map<String, Integer> mergedKeywords) {
        try {
            String keywordsJson = om.writeValueAsString(mergedKeywords);

            boolean isProd = Arrays.stream(ctx.getEnvironment().getActiveProfiles())
                    .anyMatch(p -> p.equals("prod"));

            String activationCommand = isProd ?
                    String.format("source %s/activate", properties.getBase()) :
                    String.format("%s/activate.bat", properties.getBase());

            String pythonPath = isProd ?
                    properties.getBase() + "/python" :
                    properties.getBase() + "/python.exe";

            String outputDir = fileProperties.getPath() + "/trend";

            // JSON 문자열을 파라미터로 넘기기 위해 임시 파일 사용
            File tempFile = File.createTempFile("keywords", ".json");
            try (FileWriter fw = new FileWriter(tempFile)) {
                fw.write(keywordsJson);
            }

            ProcessBuilder builder = new ProcessBuilder(
                    pythonPath,
                    properties.getTrend() + "/generate_image.py", // 새 Python 스크립트
                    outputDir,
                    tempFile.getAbsolutePath()
            );

            Process process = builder.start();

            if (process.waitFor() == 0) {
                String imageFileName = process.inputReader().lines().collect(Collectors.joining());

                // 경로 조합
                String imageUrl = request.getContextPath() + fileProperties.getUrl() + "/trend/" + imageFileName;

                return imageUrl;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
