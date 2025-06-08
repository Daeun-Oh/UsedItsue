package org.koreait.trend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.koreait.global.configs.PythonProperties;
import org.koreait.trend.entities.EtcTrend;
import org.koreait.trend.entities.NewsTrend;
import org.koreait.trend.repositories.EtcTrendRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 기타 트렌드 수집 서비스
 *
 * - 사용자가 입력한 사이트 URL로 트렌드 분석 결과를 수집
 * - Python 스크립트를 실행하고, 결과를 파싱하여 DB에 저장
 */
//@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({PythonProperties.class, FileProperties.class})
public class EtcTrendService {

    private final PythonProperties properties;  // python 실행 경로 설정
    private final FileProperties fileProperties;// 파일 경로 및 URL 설정
    private final WebApplicationContext ctx;    // Spring 컨텍스트 (환경설정 확인용)
    private final HttpServletRequest request;   // contextPath 추출용
    private final ObjectMapper om;              // JSON 변환용
    private final EtcTrendRepository etcTrendRepository;

    /**
     * Python 스크립트를 실행하여 기타 트렌드 수집 및 저장
     *
     * @param siteUrl 사용자 입력 사이트 URL
     *
     * 실행 흐름:
     * 1. 현재 환경에 따라 python 실행 경로 결정
     * 2. etc_trend.py 스크립트를 siteUrl 인자로 실행
     * 3. 결과 JSON → NewsTrend 객체로 파싱
     * 4. EtcTrend 엔티티로 변환 후 DB 저장
     */
    public void fetchAndSave(String siteUrl) {
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
                builder = new ProcessBuilder(pythonPath, properties.getTrend() + "/etc_trend.py", fileProperties.getPath() + "/trend", siteUrl);
                //System.out.println(fileProperties.getPath() + "/trend");
                process = builder.start();
                int statusCode = process.waitFor();
                if (statusCode == 0) {
                    String json = process.inputReader().lines().collect(Collectors.joining());
                    NewsTrend result = om.readValue(json, NewsTrend.class);

                    // DB 저장용 엔티티 구성
                    EtcTrend trend = new EtcTrend();
                    trend.setCategory("ETC");
                    trend.setSiteUrl(siteUrl);
                    trend.setWordCloud(fileProperties.getUrl() + "/trend/" + result.getImage());
                    trend.setKeywords(om.writeValueAsString(result.getKeywords()));

                    //System.out.println(trend);

                    // 저장
                    etcTrendRepository.save(trend);

//                    String json = process.inputReader().lines().collect(Collectors.joining());
//                    return om.readValue(json, EtcTrend.class);

                } else {
                    System.out.println("statusCode:" + statusCode);
                    process.errorReader().lines().forEach(System.out::println);
                }
            }
//            ProcessBuilder builder = new ProcessBuilder(activationCommand); // 가상환경 활성화
//            Process process = builder.start();
//            if (process.waitFor() == 0) {
//                String json = process.inputReader().lines().collect(Collectors.joining());
//                NewsTrend result = om.readValue(json, NewsTrend.class);
//
//                // DB 저장용 엔티티 구성
//                EtcTrend trend = new EtcTrend();
//                trend.setCategory("ETC");
//                trend.setSiteUrl(siteUrl);
//                trend.setWordCloud(request.getContextPath() + fileProperties.getUrl() + "/trend/" + result.getImage());
//                trend.setKeywords(om.writeValueAsString(result.getKeywords()));
//
//                System.out.println(trend);
//
//                // 저장
//                etcTrendRepository.save(trend);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 기타 트렌드 주기적 수집 작업 (스케줄러)
     *
     * - 매 1시간마다 실행됨
     * - DB에 등록된 사이트 목록을 순회하면서 트렌드 데이터 수집
     */
    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.HOURS)
//    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void scheduledEtcJob() {
        List<String> siteUrls = etcTrendRepository.findDistinctSiteUrls();
        for (String siteUrl : siteUrls) {
            fetchAndSave(siteUrl);
        }
        System.out.println("** data is saved **");
    }
}
