package org.koreait.admin.trend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.services.TrendInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/trend")
public class TrendController extends CommonController {

    private final TrendInfoService infoService;

    /**
     * 공통 모델 속성 설정 - 메인 코드 "trend"
     * (탭 메뉴 등에서 활용됨)
     */
    @Override
    @ModelAttribute("mainCode")
    public String mainCode() {
        return "trend";
    }

    /**
     * 뉴스 트렌드 페이지
     * URL: /admin/trend 또는 /admin/trend/news 요청 처리
     */
    @GetMapping({"", "/news"})
    public String news(Model model) {
        commonProcess("news", model);

        Trend item = infoService.getLatest("NEWS");
        System.out.println("item(controller): " + item);

        model.addAttribute("item", item);

        return "admin/trend/news";
    }

    /**
     * 기타 트렌드 페이지
     * 사용자가 직접 사이트 URL을 입력하여 해당 트렌드를 조회
     * URL: /admin/trend/etc
     */
    @GetMapping("/etc")
    public String etc(@ModelAttribute TrendSearch search, Model model, BindingResult result) throws Exception {
        commonProcess("etc", model);
/*

        */
/**
         * 일주일 트렌드 데이터 불러오기
         *//*


        CommonSearch commonSearch = new CommonSearch();
        commonSearch.setSDate(null);
        commonSearch.setEDate(null);

        List<Trend> items = infoService.getList("NEWS", commonSearch);

        //System.out.println("items: " + items);

        */
/* 데이터를 {날짜=Trend, 날짜=Trend, ...} 형태로 변환 *//*


        Map<String, Trend> trendMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 날짜 형식 지정

        for (Trend trend : items) {
            String dateKey = trend.getCreatedAt().format(formatter);
            trendMap.put(dateKey, trend); // 날짜 중복이 없다는 전제
        }

        //System.out.println("items(map): " + trendMap);

        */
/* Map 데이터를 json 문자열로 변환 (etc.js에서 활용) *//*


        // Jackson ObjectMapper 생성 및 설정
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDateTime 등 자바 8 날짜 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식 날짜 출력

        // Map을 JSON 문자열로 변환
        String itemsJson = mapper.writeValueAsString(trendMap);

        System.out.println("items(json): " + itemsJson);

        model.addAttribute("items", itemsJson);
*/

        /**
         * 사이트 url 입력 받고, 정보 수집 및 저장
         */

        // URL 입력 누락 또는 유효성 오류 시, 초기 화면으로 복귀
        if (!StringUtils.hasText(search.getSiteUrl()) || result.hasErrors()) {
            model.addAttribute("search", search);

            return "admin/trend/etc";
        }

        String siteUrl = search.getSiteUrl();

        //System.out.println(siteUrl);

        // 1. 입력한 사이트의 트렌드 정보 수집 및 저장
        infoService.fetchAndSaveEtcTrend(siteUrl);

        // 날짜 범위 계산
        LocalDate toDate = LocalDate.now();

        LocalDateTime weeklyFrom = toDate.minusDays(6).atStartOfDay();
        LocalDateTime weeklyTo = toDate.atTime(LocalTime.MAX);

        YearMonth yearMonth = YearMonth.now();
        LocalDateTime monthlyFrom = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthlyTo = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 병합 키워드 및 이미지 생성
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Map<String, Integer> mergedToday = infoService.getMergedTrendsForToday(siteUrl);
        Map<String, Integer> mergedWeekly = infoService.getMergedTrends(siteUrl, weeklyFrom, weeklyTo);
        Map<String, Integer> mergedMonthly = infoService.getMergedTrends(siteUrl, monthlyFrom, monthlyTo);

        String mergedTodayJson = om.writeValueAsString(mergedToday);
        String weeklyDailyJson = om.writeValueAsString(infoService.getDailyMergedKeywords(siteUrl, weeklyFrom, weeklyTo));
        String monthlyDailyJson = om.writeValueAsString(infoService.getDailyMergedKeywords(siteUrl, monthlyFrom, monthlyTo));

        String todayImagePath = infoService.generateWordCloudImage(mergedToday);
        String weeklyImagePath = infoService.generateWordCloudImage(mergedWeekly);
        String monthlyImagePath = infoService.generateWordCloudImage(mergedMonthly);

        model.addAttribute("search", search);
        model.addAttribute("today", mergedTodayJson);
        model.addAttribute("todayImage", todayImagePath);
        model.addAttribute("weekly", weeklyDailyJson);
        model.addAttribute("weeklyImage", weeklyImagePath);
        model.addAttribute("monthly", monthlyDailyJson);
        model.addAttribute("monthlyImage", monthlyImagePath);

        return "admin/trend/etc";
    }

    /**
     * 공통 처리 메서드
     * 하위 페이지에서 사용하는 스크립트, 타이틀 등을 설정
     * @param code  하위 메뉴 코드 (예: news, etc)
     * @param model 뷰에 전달할 모델
     */
    private void commonProcess(String code, Model model) {
        code = StringUtils.hasText(code) ? code : "news";

        String pageTitle = "";
        List<String> addScript = new ArrayList<>();

        if (code.equals("news")) {
            addScript.add("trend/news");
            pageTitle = "오늘의 뉴스 트렌드";
        } else if (code.equals("etc")) {
            // 팀별 소스 넣어주세요..
            addScript.add("trend/daily");
            addScript.add("trend/weekly");
            addScript.add("trend/monthly");
            pageTitle = "오늘의 사이트별 트렌드";
        }

        System.out.println(addScript);

        model.addAttribute("subCode", code);         // 서브 탭 메뉴 활성화용
        model.addAttribute("addScript", addScript);  // 필요한 JS 추가
        model.addAttribute("pageTitle", pageTitle);  // 페이지 타이틀

    }
}