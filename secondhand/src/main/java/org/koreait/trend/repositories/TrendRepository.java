package org.koreait.trend.repositories;

import org.koreait.trend.entities.Trend;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrendRepository extends ListCrudRepository<Trend, Long> {
    @Query("SELECT * FROM TREND WHERE category=:category ORDER BY createdAt DESC LIMIT 1")
    Optional<Trend> getLatest(@Param("category") String category);
}