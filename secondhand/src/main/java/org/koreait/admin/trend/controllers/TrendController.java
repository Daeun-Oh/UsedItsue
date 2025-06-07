package org.koreait.admin.trend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.services.TrendInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/trend")
public class TrendController extends CommonController {

    private final TrendInfoService infoService;

    @Override
    @ModelAttribute("mainCode")
    public String mainCode() {
        return "trend";
    }

    @GetMapping({"", "/news"}) // /admin/trend, /admin/trend/news
    public String news(Model model) {
        commonProcess("news", model);

        Trend item = infoService.getLatest("NEWS");
        System.out.println("item(controller): " + item);

        model.addAttribute("item", item);

        return "admin/trend/news";
    }

    @GetMapping("/etc")
    public String etc(@ModelAttribute TrendSearch search, Model model) throws Exception {
        commonProcess("etc", model);

        CommonSearch commonSearch = new CommonSearch();
        commonSearch.setSDate(null);
        commonSearch.setEDate(null);

        List<Trend> items = infoService.getList("NEWS", commonSearch);

        //System.out.println("items: " + items);

        /* 데이터를 {날짜=Trend, 날짜=Trend, ...} 형태로 변환 */

        Map<String, Trend> trendMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 날짜 형식 지정

        for (Trend trend : items) {
            String dateKey = trend.getCreatedAt().format(formatter);
            trendMap.put(dateKey, trend); // 날짜 중복이 없다는 전제
        }

        //System.out.println("items(map): " + trendMap);

        /* Map 데이터를 json 문자열로 변환 (etc.js에서 활용) */

        // Jackson ObjectMapper 생성 및 설정
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDateTime 등 자바 8 날짜 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 형식 날짜 출력

        // Map을 JSON 문자열로 변환
        String itemsJson = mapper.writeValueAsString(trendMap);

        //System.out.println("items(json): " + itemsJson);

        model.addAttribute("items", itemsJson);

        return "admin/trend/etc";
    }

    /**
     * 공통 처리
     *
     * @param code : 서브메뉴 코드
     * @param model
     */
    private void commonProcess(String code, Model model) {
        code = StringUtils.hasText(code) ? code : "news";

        String pageTitle = "";
        List<String> addScript = new ArrayList<>();

        if (code.equals("news")) {
            addScript.add("trend/news");
            pageTitle = "오늘의 뉴스 트렌드";
        } else if (code.equals("etc")) {
            // 팀별 소스 넣어주세요...
            addScript.add("trend/etc");
            pageTitle = "일주일 뉴스 트렌드";
        }

        model.addAttribute("subCode", code);
        model.addAttribute("addScript", addScript);
        model.addAttribute("pageTitle", pageTitle);
    }
}
