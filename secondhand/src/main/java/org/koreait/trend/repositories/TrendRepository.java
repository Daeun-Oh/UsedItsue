package org.koreait.trend.repositories;

import org.koreait.trend.entities.Trend;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrendRepository extends ListCrudRepository<Trend, Long> {
    // NEWS 카테고리 최신 트렌드 (news 페이지에서 사용)
    @Query("SELECT * FROM TREND WHERE category=:category ORDER BY createdAt DESC LIMIT 1")
    Optional<Trend> getLatest(@Param("category") String category);

    @Query(value = """
    SELECT * FROM TREND
    WHERE category = :category
      AND DATE(createdAt) BETWEEN :sDate AND :eDate
    ORDER BY createdAt ASC
    """)
    List<Trend> getList(
            @Param("category") String category,
            @Param("sDate") LocalDate sDate,
            @Param("eDate") LocalDate eDate
    );
}
