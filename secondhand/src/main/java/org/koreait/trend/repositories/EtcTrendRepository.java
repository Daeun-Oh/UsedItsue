package org.koreait.trend.repositories;

import org.koreait.trend.entities.EtcTrend;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EtcTrendRepository extends ListCrudRepository<EtcTrend, Long> {
    /**
     * 오늘 등록된 트렌드 중 최신 1건 조회
     *
     * @param siteUrl 조회할 사이트 URL
     * @param from 시작 시간 (보통 오늘 00:00)
     * @param to 종료 시간 (보통 오늘 23:59)
     * @return 최신 1건의 트렌드 데이터 (없으면 Optional.empty())
     */
    @Query("SELECT * FROM ETC_TREND WHERE siteUrl = :siteUrl AND createdAt BETWEEN :from AND :to ORDER BY createdAt DESC LIMIT 1")
    Optional<EtcTrend> findFirstBySiteUrlAndCreatedAtBetween(
            @Param("siteUrl") String siteUrl,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * 지정 기간 동안의 트렌드 데이터 전체 조회
     *
     * @param siteUrl 조회할 사이트 URL
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @return 기간 내 등록된 트렌드 리스트 (등록 순)
     */
    @Query("SELECT * FROM ETC_TREND WHERE siteUrl = :siteUrl AND createdAt BETWEEN :from AND :to ORDER BY createdAt ASC")
    List<EtcTrend> findBySiteUrlAndCreatedAtBetween(
            @Param("siteUrl") String siteUrl,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * 등록된 사이트 URL 목록 조회 (중복 제거)
     *
     * @return DISTINCT siteUrl 리스트
     */
    @Query("SELECT DISTINCT siteUrl FROM ETC_TREND")
    List<String> findDistinctSiteUrls();
}
