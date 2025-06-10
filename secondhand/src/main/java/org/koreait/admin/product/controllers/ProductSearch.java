package org.koreait.admin.product.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.koreait.product.constants.ProductStatus;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("PRODUCT")
public class ProductSearch {
    @NotBlank
    private String name;
    private String category;
    private ProductStatus status;
}
