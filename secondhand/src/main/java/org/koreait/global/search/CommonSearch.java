package org.koreait.global.search;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommonSearch {
    private LocalDate sDate; // 검색 시작일
    private LocalDate eDate; // 검색 종료일
}
