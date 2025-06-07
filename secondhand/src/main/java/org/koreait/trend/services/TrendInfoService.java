package org.koreait.trend.services;

import lombok.RequiredArgsConstructor;
import org.koreait.global.search.CommonSearch;
import org.koreait.trend.entities.Trend;
import org.koreait.trend.exceptions.TrendNotFoundException;
import org.koreait.trend.repositories.TrendRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class TrendInfoService {

    private final TrendRepository repository;

    /**
     * 최근 트렌드 1개 조회
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
}
