package org.koreait.trend.repositories;

import org.junit.jupiter.api.Test;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.EtcTrend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@SpringBootTest
public class EtcTrendRepositoryTest {

    @Autowired
    private EtcTrendRepository etcTrendRepository;

    @Test
    void test01() {  //일주일
        String siteUrl = "https://news.naver.com/section/103";

        CommonSearch search = new CommonSearch();
        search.setSiteUrl(siteUrl);
        search.setSDate(LocalDate.now().minusDays(6));
        search.setEDate(LocalDate.now());

        List<EtcTrend> trends = etcTrendRepository.getKeywordsBetween(search.getSiteUrl(), search.getSDate().atStartOfDay(), search.getEDate().atTime(LocalTime.MAX));

        System.out.println("========================"+ search.getSiteUrl() +", "+ search.getSDate() +", "+ search.getEDate() + "==================");
        System.out.println(trends);
    }

    @Test
    void test02() {  //이번달
        String siteUrl = "https://news.naver.com/section/103";

        CommonSearch search = new CommonSearch();
        search.setSiteUrl(siteUrl);
        search.setSDate(YearMonth.now().atDay(1));

        List<EtcTrend> trends = etcTrendRepository.getKeywordsBetween(search.getSiteUrl(), search.getSDate().atStartOfDay(), search.getEDate().atTime(LocalTime.MAX));

        System.out.println("========================"+ search.getSiteUrl() +", "+ search.getSDate() +", "+ search.getEDate() + "==================");
        System.out.println(trends);
    }
}
