package org.koreait.product.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.koreait.product.constants.ProductStatus;

@Data
public class RequestProduct {
    private String mode;    // add - 추가, edit - 수정

    private Long seq;       // 상품 수정할 때 필요 -> '수정 모드'일 때만 사용

    private String gid;

    @NotBlank
    private String name;

    private String category;

    @NotNull
    private ProductStatus status;

    private int consumerPrice;

    private int salePrice;

    private String description;
}
