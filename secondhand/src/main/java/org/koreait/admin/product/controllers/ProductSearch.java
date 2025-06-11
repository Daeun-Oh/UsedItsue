package org.koreait.admin.product.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.koreait.global.search.CommonSearch;
import org.koreait.product.constants.ProductStatus;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("PRODUCT")
public class ProductSearch extends CommonSearch {
    @NotBlank
    private String name;
    private String category;
    private ProductStatus status;
}
