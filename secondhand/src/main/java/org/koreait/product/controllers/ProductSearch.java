package org.koreait.product.controllers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.koreait.global.search.CommonSearch;
import org.koreait.product.constants.ProductStatus;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSearch extends CommonSearch {
    private List<ProductStatus> statusList;
}
