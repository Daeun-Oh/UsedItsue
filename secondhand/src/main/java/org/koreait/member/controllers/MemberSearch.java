package org.koreait.member.controllers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.koreait.global.search.CommonSearch;
import org.koreait.member.constants.Authority;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberSearch extends CommonSearch {
    private List<Authority> authorityList;
}
