package org.koreait.trend.repositories;

import org.junit.jupiter.api.Test;
import org.koreait.trend.entities.EtcTrend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        LocalDate weeklyToday = LocalDate.now();
        LocalDate weekAgo = weeklyToday.minusDays(6);

        LocalDateTime weeklyFrom = weekAgo.atStartOfDay();              // 7일 전 00:00
        LocalDateTime weeklyTo = weeklyToday.atTime(LocalTime.MAX);     // 오늘 23:59

        List<EtcTrend> trends = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, weeklyFrom, weeklyTo);

        System.out.println("========================"+ weeklyToday +", "+ weeklyFrom +", "+ weeklyTo + "==================");
        System.out.println(trends);

    }

    @Test
    void test02() {  //이번달
        String siteUrl = "https://news.naver.com/section/103";

        YearMonth yearMonth = YearMonth.now();

        LocalDate fromDate = yearMonth.atDay(1); // 1일
        LocalDate toDate = yearMonth.atEndOfMonth(); // 말일

        LocalDateTime monthlyFrom = fromDate.atStartOfDay();            // 00:00:00
        LocalDateTime monthlyTo = toDate.atTime(LocalTime.MAX);         // 23:59:59.999...

        List<EtcTrend> trends = etcTrendRepository.findBySiteUrlAndCreatedAtBetween(siteUrl, monthlyFrom, monthlyTo);
        System.out.println("========================"+ yearMonth +", "+ monthlyFrom +", "+ monthlyTo + "==================");
        System.out.println(trends);

    }
}
