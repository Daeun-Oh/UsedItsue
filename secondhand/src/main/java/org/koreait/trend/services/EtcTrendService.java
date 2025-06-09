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
 * 기타 트렌드 수집 및 처리
 * - Python 스크립트를 실행
 */
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
     * 기타 트렌드 데이터 생성 및 추가
     *
     * - etc_trend.py를 가상환경에서 실행하여 JSON 문자열 받아 옴
     * - JSON 문자열 데이터를 NewsTrend로 파싱
     * - EtcTrend 객체(데이터)를 구성
     * - ETC_TREND 테이블(DB)에 데이터 저장
     *
     * @param siteUrl : 사용자가 입력한 url
     */
    public void process(String siteUrl) {
        boolean isProduction = Arrays.stream(ctx.getEnvironment().getActiveProfiles()).anyMatch(s -> s.equals("prod"));

        // 가상환경 경로, 파이썬 프로그램 경로 저장
        String activationCommand = null, pythonPath = null;
        if (isProduction) { // 리눅스 환경, 서비스 환경
            activationCommand = String.format("source %s/activate", properties.getBase());
            pythonPath = properties.getBase() + "/python";
        } else { // 윈도우즈 환경
            activationCommand = String.format("%s/activate.bat", properties.getBase());
            pythonPath = properties.getBase() + "/python.exe";
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(activationCommand);
            Process process = builder.start();  // 가상환경 활성화

            if (process.waitFor() == 0) {  // 0: 정상 수행된 경우

                builder = new ProcessBuilder(pythonPath, properties.getTrend() + "/etc_trend.py", fileProperties.getPath() + "/trend", siteUrl);
                process = builder.start();  // 파이썬 파일(etc_trend.py) 실행

                int statusCode = process.waitFor();
                if (statusCode == 0) {  // 0: 정상 수행된 경우

                    // etc_trend.py의 print(json.dumps(data, ensure_ascii=False))를 읽어 옴
                    // (만약 파이썬에서 출력되는 내용이 여러 개면, 한 줄씩 읽어서 하나의 JSON 문자열로 결합한다)
                    String json = process.inputReader().lines().collect(Collectors.joining());

                    // 읽어 온 json 문자열을 NewTrend 객체로 변환
                    NewsTrend result = om.readValue(json, NewsTrend.class);

                    // DB 저장용 엔티티 생성 및 세팅
                    EtcTrend trend = new EtcTrend();
                    trend.setCategory("ETC");
                    trend.setSiteUrl(siteUrl);
                    trend.setWordCloud(fileProperties.getUrl() + "/trend/" + result.getImage());
                    trend.setKeywords(om.writeValueAsString(result.getKeywords()));

                    // 엔티티를 DB에 저장 (자동 매핑)
                    etcTrendRepository.save(trend);

                } else {
                    System.out.println("statusCode:" + statusCode);
                    process.errorReader().lines().forEach(System.out::println);  // 에러 스트림 출력
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ETC_TREND에 저장될 트렌드 데이터를 주기적으로 수집 (스케줄러)
     *
     * - MvcConfig의 @EnableScheduling 어노테이션 필수!
     * - 1시간마다 실행
     * - DB에 등록된 모든 사이트의 데이터 가져오기
     */
    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.HOURS)
    public void scheduledJob() {
        List<String> siteUrls = etcTrendRepository.findDistinctUrlList();
        for (String siteUrl : siteUrls) {
            process(siteUrl);
        }
        System.out.println("★ DB에 저장된 모든 사이트의 데이터 수집 완료 ★");
    }
}
