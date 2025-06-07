package org.koreait.trend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.koreait.global.configs.PythonProperties;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.EtcTrend;
import org.koreait.trend.entities.NewsTrend;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.exceptions.TrendNotFoundException;
import org.koreait.trend.repositories.EtcTrendRepository;
import org.koreait.trend.repositories.TrendRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 트렌드 정보 서비스
 *
 * - 뉴스 및 기타 트렌드 데이터 조회
 * - Python 스크립트를 통한 트렌드 수집 및 저장
 */
@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({PythonProperties.class, FileProperties.class})
public class TrendInfoService {

    private final TrendRepository repository;            // 뉴스 트렌드 전용 레포지토리
    private final EtcTrendRepository etcTrendRepository; // 기타 트렌드 전용 레포지토리
    private final WebApplicationContext ctx;             // Spring 환경 프로파일 확인용
    private final PythonProperties properties;           // Python 경로 설정
    private final FileProperties fileProperties;         // 이미지 저장 경로 설정
    private final ObjectMapper om;                       // JSON 변환기
    private final HttpServletRequest request;            // contextPath 참조용

    public Trend getLatest(String category) {
        Trend item = repository.getLatest(category).orElseThrow(TrendNotFoundException::new);
        System.out.println("item: " + item);
      
        return item;
    }

    /**
     * 특정 날짜의 트렌드 데이터 1개
     * @param date
     * @return
    */
    public Trend get(String category, LocalDate date) {
        return null;
    }

    /**
     * 특정 날짜 범위의 트렌트 데이터 조회
     *
     * @return
    */
    public List<Trend> getList(String category, CommonSearch search) {
        LocalDate sDate = search.getSDate();
        LocalDate eDate = search.getEDate();

        // 날짜가 없는 경우 기본값 설정 (최근 7일)
        if (sDate == null) sDate = LocalDate.now().minusDays(6);
        if (eDate == null) eDate = LocalDate.now();

        //System.out.println("sDate:" + sDate + " eDate:" + eDate);
        List<Trend> items =  repository.getList(category, sDate, eDate);

        return items;
    }

    /**
     * 오늘의 기타 트렌드 1건 조회
     *
     * @param siteUrl 사이트 주소
     * @return 오늘 등록된 트렌드 1건 (없을 경우 null)
     */
    public EtcTrend getTodayTrend(String siteUrl) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        return etcTrendRepository.findFirstBySiteUrlAndCreatedAtBetween(siteUrl, from, to).orElse(null);
    }

    /**
     * 특정 기간의 기타 트렌드 조회
     *
     * @param siteUrl 사이트 주소
     * @param days 조회할 기간 (예: 7일, 30일 등)
     * @return 해당 기간 동안의 트렌드 목록
     */
    // 일주일/한달 트렌드 조회
    public List<EtcTrend> getTrendsInRange(String siteUrl, int days) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(days).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        return etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, from, to);
    }

    /**
     * Python 스크립트를 실행하여 기타 트렌드 수집 및 저장
     *
     * @param siteUrl 사용자 입력 사이트 주소
     *
     * 실행 흐름:
     * 1. Python 가상환경 활성화 (운영/개발 구분)
     * 2. etc_trend.py 실행
     * 3. 결과 JSON → NewsTrend로 파싱
     * 4. EtcTrend로 변환 후 DB 저장
     */
    public void fetchAndSaveEtcTrend(String siteUrl) {
        boolean isProd = Arrays.stream(ctx.getEnvironment().getActiveProfiles()).anyMatch(p -> p.equals("prod"));

        // 가상환경 activate 명령어 설정
        String activationCommand = isProd ?
                String.format("source %s/activate", properties.getBase()) :
                String.format("%s/activate.bat", properties.getBase());

        // Python 실행 경로 설정
        String pythonPath = isProd ?
                properties.getBase() + "/python" :
                properties.getBase() + "/python.exe";

        try {
            // 1단계: 가상환경 활성화
            ProcessBuilder builder = new ProcessBuilder(activationCommand);
            Process process = builder.start();
            if (process.waitFor() != 0) return;

            // 2단계: Python 스크립트 실행
            builder = new ProcessBuilder(
                    pythonPath,
                    properties.getTrend() + "/etc_trend.py",
                    fileProperties.getPath() + "/trend",
                    siteUrl
            );
            process = builder.start();

            // 3단계: 실행 결과 파싱 및 저장
            if (process.waitFor() == 0) {
                String json = process.inputReader().lines().collect(Collectors.joining());
                NewsTrend result = om.readValue(json, NewsTrend.class);

                EtcTrend trend = new EtcTrend();
                trend.setCategory("ETC");
                trend.setSiteUrl(siteUrl);
                trend.setWordCloud(request.getContextPath() + fileProperties.getUrl() + "/trend/" + result.getImage());
                trend.setKeywords(om.writeValueAsString(result.getKeywords()));

                etcTrendRepository.save(trend);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}