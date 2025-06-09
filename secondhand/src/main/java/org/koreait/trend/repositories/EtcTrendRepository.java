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
     * 주어진 기간에 등록된 트렌드 중 최신 1건 조회
     *
     * @param siteUrl : site url
     * @param from : 범위 시작 시각
     * @param to : 범위 종료 시각
     * @return : 최신 1건의 트렌드 데이터 (없으면 Optional.empty())
     */
    @Query("SELECT * FROM ETC_TREND WHERE siteUrl = :siteUrl AND createdAt BETWEEN :from AND :to ORDER BY createdAt DESC LIMIT 1")
    Optional<EtcTrend> getLatestTrendBetween(
            @Param("siteUrl") String siteUrl,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * 지정 기간 동안의 트렌드 데이터 전체 조회
     *
     * @param siteUrl : site url
     * @param from : 범위 시작 시각
     * @param to : 범위 종료 시각
     * @return : 기간 내 등록된 트렌드 리스트 (생성 날짜 기준 오름차순)
     */
    @Query("SELECT * FROM ETC_TREND WHERE siteUrl = :siteUrl AND createdAt BETWEEN :from AND :to ORDER BY createdAt ASC")
    List<EtcTrend> getKeywordsBetween(
            @Param("siteUrl") String siteUrl,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * DB에 등록된 사이트 URL 목록 조회 (중복 제거)
     *
     * @return : DB에 등록된 url 리스트
     */
    @Query("SELECT DISTINCT siteUrl FROM ETC_TREND")
    List<String> findDistinctUrlList();
}
