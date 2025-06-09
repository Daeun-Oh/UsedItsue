package org.koreait.admin.trend.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ECT_TREND")
public class TrendSearch {
    @Column("siteUrl")
    @NotBlank
    private String siteUrl;
}
