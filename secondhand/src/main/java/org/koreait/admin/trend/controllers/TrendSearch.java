package org.koreait.admin.trend.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ECT_TREND")
public class TrendSearch {
    @Column("siteUrl")
    @NotBlank(message = "사이트 주소는 필수입니다.")
    private String siteUrl;
}
