package org.koreait.trend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.koreait.global.configs.FileProperties;
import org.koreait.global.configs.PythonProperties;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.EtcTrend;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.exceptions.TrendNotFoundException;
import org.koreait.trend.repositories.EtcTrendRepository;
import org.koreait.trend.repositories.TrendRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 트렌드 정보 조회 서비스
 * - 트렌드 데이터를 조건에 맞게 가공해서 반환하는 메서드들
 */
@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties({PythonProperties.class, FileProperties.class})
public class EtcTrendInfoService {

    private final TrendRepository repository;
    private final EtcTrendRepository etcTrendRepository;
    private final ObjectMapper om;
    private final EtcTrendService etcTrendService;

    /**
     * 특정 카테고리의 최신 트렌드 데이터 1건 반환
     *
     * @param category
     * @return
     */
    public Trend getLatest(String category) {
        Trend item = repository.getLatest(category).orElseThrow(TrendNotFoundException::new);
        System.out.println("item: " + item);

        return item;
    }

    /**
     * 특정 날짜 범위의 트렌트 데이터 반환
     *
     * @param category
     * @param search
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
     * 주어진 기간의 기타 트렌드 1건 반환
     *
     * @param search : url + 검색 기간
     * @return : 최신 트렌드 1건
     */
    public EtcTrend get(CommonSearch search) {
        String siteUrl = search.getSiteUrl();
        LocalDateTime sTime = search.getSDate().atStartOfDay();
        LocalDateTime eTime = search.getEDate().atTime(LocalTime.MAX);

        EtcTrend trend = etcTrendRepository.getLatestTrendBetween(siteUrl, sTime, eTime).orElse(null);

        return trend;
    }

    /**
     * 주어진 기간의 모든 데이터가 병합(누적)된 데이터 반환
     *
     * @param search : url + 검색 기간
     * @return : 병합된 데이터
     * @throws JsonProcessingException
     */
    public Map<String, Integer> getMergedKeywords(CommonSearch search) throws JsonProcessingException {
        String siteUrl = search.getSiteUrl();
        LocalDateTime sTime = search.getSDate().atStartOfDay();
        LocalDateTime eTime = search.getEDate().atTime(LocalTime.MAX);

        // DB에 저장된 데이터 중 해당 사이트의 모든 데이터를 createdAt 기준 오름차순된 EtcTrend 리스트를 불러온다
        List<EtcTrend> trends = etcTrendRepository.getKeywordsBetween(siteUrl, sTime, eTime);

        Map<String, Integer> mergedKeywords = new LinkedHashMap<>();

        for (EtcTrend trend : trends) {
            // trend의 keywords(map 형식의 json 문자열)를 Map<String, Integer>로 파싱
            Map<String, Integer> keywords = om.readValue(trend.getKeywords(), Map.class);

            for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                // 키워드가 있으면 값을 누적(합산), 없으면 새로 추가
                mergedKeywords.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        return mergedKeywords;
    }

    /**
     * 주어진 기간의 날짜별 데이터 반환 (해당 날짜의 최신 데이터)
     *
     * @param search : url + 검색 기간
     * @return : 날짜별 데이터
     */
    public Map<String, EtcTrend> getKeywordsBetween(CommonSearch search) {
        String siteUrl = search.getSiteUrl();
        LocalDateTime sTime = search.getSDate().atStartOfDay();
        LocalDateTime eTime = search.getEDate().atTime(LocalTime.MAX);

        List<EtcTrend> items = etcTrendRepository.getKeywordsBetween(siteUrl, sTime, eTime);

        Map<String, EtcTrend> trendMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 날짜 형식 지정

        for (EtcTrend trend : items) {
            String dateKey = trend.getCreatedAt().format(formatter);
            trendMap.put(dateKey, trend); // 날짜 중복이 없다는 전제
        }

        return trendMap;
    }
}