package org.koreait.trend.entities;

import lombok.Data;
import org.koreait.global.entities.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ETC_TREND")
public class EtcTrend extends BaseEntity {
    @Id
    private Long seq;
    private String category;

    @Column("siteUrl")
    private String siteUrl;

    @Column("wordCloud")
    private String wordCloud;

    private String keywords;
}
