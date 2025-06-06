package org.koreait.trend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.koreait.global.configs.PythonProperties;
import org.koreait.trend.entities.NewsTrend;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.repositories.TrendRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({PythonProperties.class, FileProperties.class})
public class NewsTrendService {

    private final PythonProperties properties;
    private final FileProperties fileProperties;

    private final WebApplicationContext ctx;
    private final TrendRepository repository;
    private final HttpServletRequest request;
    private final ObjectMapper om;

    public NewsTrend process() {
        // spring.profiles.active=default,prod
        boolean isProduction = Arrays.stream(ctx.getEnvironment().getActiveProfiles()).anyMatch(s -> s.equals("prod"));

        String activationCommand = null, pythonPath = null;
        if (isProduction) { // 리눅스 환경, 서비스 환경
            activationCommand = String.format("source %s/activate", properties.getBase());
            pythonPath = properties.getBase() + "/python";
        } else { // 윈도우즈 환경
            activationCommand = String.format("%s/activate.bat", properties.getBase());
            pythonPath = properties.getBase() + "/python.exe";
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(activationCommand); // 가상환경 활성화
            Process process = builder.start();
            if (process.waitFor() == 0) { // 정상 수행된 경우
                builder = new ProcessBuilder(pythonPath, properties.getTrend() + "/trend.py", fileProperties.getPath() + "/trend");
                process = builder.start();
                int statusCode = process.waitFor();
                if (statusCode == 0) {
                    String json = process.inputReader().lines().collect(Collectors.joining());
                    return om.readValue(json, NewsTrend.class);

                } else {
                    System.out.println("statusCode:" + statusCode);
                    process.errorReader().lines().forEach(System.out::println);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // 실패시에는 null
    }

    /**
     * 매 1시간 마다 주기적으로 뉴스 트렌드 데이터를 저장
     *
     */
    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.HOURS)
    public void scheduledJob() {
       NewsTrend item = process();
       if (item == null) return;
        String wordCloud = String.format("%s%s/trend/%s", request.getContextPath(), fileProperties.getUrl(), item.getImage());

        try {
            String keywords = om.writeValueAsString(item.getKeywords());
            Trend data = new Trend();
            data.setCategory("NEWS");
            data.setWordCloud(wordCloud);
            data.setKeywords(keywords);
            repository.save(data);
        } catch (JsonProcessingException e) {}
    }
}
