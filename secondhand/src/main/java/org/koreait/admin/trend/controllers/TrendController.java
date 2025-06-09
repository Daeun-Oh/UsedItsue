package org.koreait.admin.trend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.services.EtcTrendService;
import org.koreait.trend.services.EtcTrendImageService;
import org.koreait.trend.services.EtcTrendInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/trend")
public class TrendController extends CommonController {

    private final EtcTrendInfoService infoService;
    private final EtcTrendService trendService;
    private final EtcTrendImageService imageService;
    private final ObjectMapper om;

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

        model.addAttribute("item", item);

        return "admin/trend/news";
    }

    /**
     * 기타 트렌드 페이지
     * 사용자가 직접 사이트 URL을 입력하여 해당 트렌드를 조회
     * URL: /admin/trend/etc
     *
     * @param search : 사용자가 입력한 url (클라이언트가 보낸 데이터를 TrendSearch 객체에 자동 매핑)
     * @param errors : @ModelAttribue로 바인딩할 때 Validation을 거치면, 검증 오류가 이 객체에 저장됨
     * @param model : 컨트롤러가 View로 데이터 전달 시 사용
     * @return : View 경로
     * @throws Exception
     */
    @GetMapping("/etc")
    public String etc(@ModelAttribute TrendSearch search, Errors errors, Model model) throws Exception {
        commonProcess("etc", model);

        // URL 입력 누락 또는 유효성 오류 시, 초기 화면으로 복귀
        if (!StringUtils.hasText(search.getSiteUrl()) || errors.hasErrors()) {
            model.addAttribute("search", search);

            return "admin/trend/etc";
        }

        String siteUrl = search.getSiteUrl();

        // 입력한 사이트의 트렌드 정보 수집 및 저장
        trendService.process(siteUrl);

        // 오늘 날짜
        LocalDate today = LocalDate.now();

        // weekly 시작 날짜 (6일 전)
        LocalDate weeklyStart = today.minusDays(6);

        // monthly 시작 날짜 (이번 달 1일)
        LocalDate monthlyStart = today.withDayOfMonth(1);

        /* 병합된 키워드 데이터 및 워드 클라우드 생성 S */
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Jackson이 날짜를 timestamp(숫자/배열) 형식으로 직렬화하는 것을 방지

        CommonSearch searchForm = new CommonSearch();
        searchForm.setSiteUrl(siteUrl);
        searchForm.setEDate(today);

        // 1. Today
        searchForm.setSDate(today);

        Map<String, Integer> mergedToday = infoService.getMergedKeywords(searchForm);
        String todayImagePath = imageService.process(mergedToday);
        String mergedTodayJson = om.writeValueAsString(mergedToday);

        // 2. weekly
        searchForm.setSDate(weeklyStart);
        Map<String, Integer> mergedWeekly = infoService.getMergedKeywords(searchForm);
        String weeklyImagePath = imageService.process(mergedWeekly);
        String weeklyDailyJson = om.writeValueAsString(infoService.getKeywordsBetween(searchForm));

        // 3. monthly
        searchForm.setSDate(monthlyStart);
        Map<String, Integer> mergedMonthly = infoService.getMergedKeywords(searchForm);
        String monthlyImagePath = imageService.process(mergedMonthly);
        String monthlyDailyJson = om.writeValueAsString(infoService.getKeywordsBetween(searchForm));
        /* 병합된 키워드 데이터 및 워드 클라우드 생성 E */

        model.addAttribute("search", search);
        model.addAttribute("today", mergedTodayJson);
        model.addAttribute("todayImagePath", todayImagePath);
        model.addAttribute("weekly", weeklyDailyJson);
        model.addAttribute("weeklyImagePath", weeklyImagePath);
        model.addAttribute("monthly", monthlyDailyJson);
        model.addAttribute("monthlyImagePath", monthlyImagePath);

        return "admin/trend/etc";
    }

    /**
     * 공통 처리 메서드
     * 하위 페이지에서 사용하는 스크립트, 타이틀 등을 설정
     *
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
            addScript.add("trend/daily");
            addScript.add("trend/weekly");
            addScript.add("trend/monthly");
            pageTitle = "오늘의 사이트별 트렌드";
        }

        model.addAttribute("subCode", code);         // 서브 탭 메뉴 활성화용
        model.addAttribute("addScript", addScript);  // 필요한 JS 추가
        model.addAttribute("pageTitle", pageTitle);  // 페이지 타이틀

    }
}