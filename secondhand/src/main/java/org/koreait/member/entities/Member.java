package org.koreait.member.entities;

import lombok.Data;
import org.koreait.global.entities.BaseEntity;
import org.koreait.member.constants.Authority;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("MEMBER")
public class Member extends BaseEntity {
    @Id
    private Long seq;
    private String email;
    private String password;
    private String name;
    private String mobile;
    private Authority authority = Authority.MEMBER;

    @Column("termsAgree")
    private boolean termsAgree;

    private boolean locked; // 계정 중지 상태인지
    private LocalDateTime expired; // 계정 만료 일자, null이면 만료 X

    @Column("credentialChangedAt")
    private LocalDateTime credentialChangedAt; // 비밀번호 변경 일시
}
