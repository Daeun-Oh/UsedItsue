package org.koreait.admin.trend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.trend.entities.EtcTrend;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.services.TrendInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;


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
        model.addAttribute("item", item);

        return "admin/trend/news";
    }

    /**
     * 기타 트렌드 페이지
     * 사용자가 직접 사이트 URL을 입력하여 해당 트렌드를 조회
     * URL: /admin/trend/etc
     */
    @GetMapping("/etc")
    public String etc(@ModelAttribute TrendSearch search, Model model, BindingResult result) {
        // 공통 처리 (타이틀, 스크립트 등)
        commonProcess("etc", model);
        // URL 입력 누락 또는 유효성 오류 시, 초기 화면으로 복귀
        if (!StringUtils.hasText(search.getSiteUrl()) || result.hasErrors()) {
            model.addAttribute("search", search);

            return "admin/trend/etc";
        }

        String siteUrl = search.getSiteUrl();

        // 1. 입력한 사이트의 트렌드 정보 수집 및 저장
        infoService.fetchAndSaveEtcTrend(siteUrl);

        // 오늘 데이터 1개
        EtcTrend today = infoService.getTodayTrend(siteUrl);

        // 일주일간 누적
        List<EtcTrend> weekly = infoService.getTrendsInRange(siteUrl, 7);

        // 한달간 누적
        List<EtcTrend> monthly = infoService.getTrendsInRange(siteUrl, 30);

        // 5. 모델에 결과 데이터 담기
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화를 위해 필요
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날짜를 문자열로

        model.addAttribute("search", search);
        model.addAttribute("today", today);
        model.addAttribute("weeklyList", weekly);
        model.addAttribute("monthlyList", monthly);

        try {
            model.addAttribute("weekly", om.writeValueAsString(weekly));
            model.addAttribute("monthly", om.writeValueAsString(monthly));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("weekly", "[]");
            model.addAttribute("monthly", "[]");
        }

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
            addScript.add("trend/news"); // /static/js/trend/news.js
            pageTitle = "오늘의 뉴스 트렌드";
        } else if (code.equals("etc")) {
            // 팀별 소스 넣어주세요...
            addScript.add("trend/etc"); // /static/js/trend/etc.js
            pageTitle = "오늘의 사이트별 트렌드";
        }

        model.addAttribute("subCode", code);         // 서브 탭 메뉴 활성화용
        model.addAttribute("addScript", addScript);  // 필요한 JS 추가
        model.addAttribute("pageTitle", pageTitle);  // 페이지 타이틀

    }
}