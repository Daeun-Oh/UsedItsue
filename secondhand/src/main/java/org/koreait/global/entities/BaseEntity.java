package org.koreait.global.entities;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * 모든 Entity(Table)에 있는
 * 생성 날짜, 수정 날짜, 삭제 날짜 컬럼
 */
@Data
public abstract class BaseEntity {
    @CreatedDate
    @Column("createdAt")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("modifiedAt")
    private LocalDateTime modifiedAt;

    @Column("deletedAt")
    private LocalDateTime deletedAt;
}
