package org.koreait.trend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final EtcTrendService etcTrendService;

    public Trend getLatest(String category) {
        Trend item = repository.getLatest(category).orElseThrow(TrendNotFoundException::new);
        System.out.println("item: " + item);

        return item;
    }

    /**
     * 특정 날짜의 트렌드 데이터 1개
     *
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
        String siteUrl = search.getSiteUrl();
        LocalDate sDate = search.getSDate();
        LocalDate eDate = search.getEDate();

        // 날짜가 없는 경우 기본값 설정 (최근 7일)
        if (sDate == null) sDate = LocalDate.now().minusDays(6);
        if (eDate == null) eDate = LocalDate.now();

        //System.out.println("sDate:" + sDate + " eDate:" + eDate);
        List<Trend> items = repository.getList(category, sDate, eDate, siteUrl);

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

        EtcTrend trend = etcTrendRepository.findFirstBySiteUrlAndCreatedAtBetween(siteUrl, from, to).orElse(null);

        if (trend == null) {
            // 없으면 해당 url로 새로운 데이터 생성
            etcTrendService.fetchAndSave(siteUrl);
            trend = etcTrendRepository.findFirstBySiteUrlAndCreatedAtBetween(siteUrl, from, to).orElse(null);
        }

        return trend;
    }

    /**
     * 특정 기간의 기타 트렌드 조회
     *
     * @param siteUrl 사이트 주소
     * @param days    조회할 기간 (예: 7일, 30일 등)
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
     * 실행 흐름:
     * 1. Python 가상환경 활성화 (운영/개발 구분)
     * 2. etc_trend.py 실행(siteUrl 기준)
     * 3. 결과 JSON → etcTrend로 파싱
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

    /**
     * 오늘 생성된 기타 트렌드 데이터를 키워드 기준으로 병합
     */
    public Map<String, Integer> getMergedTrendsForToday(String siteUrl) throws JsonProcessingException {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        List<EtcTrend> todayTrends = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, todayStart, todayEnd);

        Map<String, Integer> mergedKeywords = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        for (EtcTrend trend : todayTrends) {
            Map<String, Integer> keywords = mapper.readValue(trend.getKeywords(), Map.class);

            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();

                mergedKeywords.put(key, mergedKeywords.getOrDefault(key, 0) + value);
            }
        }

        return mergedKeywords;
    }

    /**
     * 병합된 키워드 데이터로 워드클라우드 이미지 생성
     * - generate_image.py 호출
     * - 결과 이미지 파일명을 반환
     */
    public String generateWordCloudImage(Map<String, Integer> mergedKeywords) {
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

    /**
     * 주어진 기간 동안의 키워드 데이터를 병합
     */
    public Map<String, Integer> getMergedTrends(String siteUrl, LocalDateTime from, LocalDateTime to) throws JsonProcessingException {
        List<EtcTrend> trends = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, from, to);

        Map<String, Integer> mergedKeywords = new LinkedHashMap<>();
        for (EtcTrend trend : trends) {
            Map<String, Integer> keywords = om.readValue(trend.getKeywords(), Map.class);
            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                mergedKeywords.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        return mergedKeywords;
    }

    public Map<String, EtcTrend> getDailyKeywords(String siteUrl, LocalDateTime from, LocalDateTime to) throws JsonProcessingException {

        List<EtcTrend> items = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, from, to);

        /* 데이터를 {날짜=Trend, 날짜=Trend, ...} 형태로 변환 */


        Map<String, EtcTrend> trendMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 날짜 형식 지정

        for (EtcTrend trend : items) {
            String dateKey = trend.getCreatedAt().format(formatter);
            trendMap.put(dateKey, trend); // 날짜 중복이 없다는 전제
        }

        //System.out.println("items(map): " + trendMap);


        /* Map 데이터를 json 문자열로 변환 */
        om.registerModule(new JavaTimeModule()); // LocalDateTime 등 자바 8 날짜 지원
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식 날짜 출력

        // Map을 JSON 문자열로 변환
        String itemsJson = om.writeValueAsString(trendMap);

        System.out.println("items(json): " + itemsJson);

        return trendMap;
    }

    /**
     * 특정 기간 동안 일자별로 키워드를 병합하여 반환
     *
     * @param siteUrl 사이트 주소
     * @param from    조회 시작일 (포함)
     * @param to      조회 종료일 (포함)
     * @return 일자별 병합된 키워드 맵 (예: {"2025-06-01": {...}, "2025-06-02": {...}})
     */
    public Map<String, EtcTrend> getDailyMergedKeywords(String siteUrl, LocalDateTime from, LocalDateTime to) throws JsonProcessingException {
        List<EtcTrend> trends = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, from, to);

        Map<String, EtcTrend> result = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> keywordMapByDate = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (EtcTrend trend : trends) {
            String dateKey = trend.getCreatedAt().format(formatter);

            // 날짜 기준 가장 마지막 trend 저장
            result.put(dateKey, trend);

            // 키워드 병합
            Map<String, Integer> keywords = om.readValue(trend.getKeywords(), Map.class);
            keywordMapByDate.computeIfAbsent(dateKey, k -> new LinkedHashMap<>());

            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                Map<String, Integer> dailyKeywords = keywordMapByDate.get(dateKey);
                dailyKeywords.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        // 병합된 키워드를 JSON 문자열로 저장
        for (Map.Entry<String, EtcTrend> entry : result.entrySet()) {
            String dateKey = entry.getKey();
            EtcTrend trend = entry.getValue();
            Map<String, Integer> mergedKeywords = keywordMapByDate.get(dateKey);

            // keyword JSON 문자열로 다시 세팅
            trend.setKeywords(om.writeValueAsString(mergedKeywords));
        }

        return result; // ObjectMapper로 JSON 직렬화하면 원하는 형식 나옴
    }
}