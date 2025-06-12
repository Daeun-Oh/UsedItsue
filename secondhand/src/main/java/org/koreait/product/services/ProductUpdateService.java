package org.koreait.product.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.script.AlertException;
import org.koreait.global.libs.Utils;
import org.koreait.product.constants.ProductStatus;
import org.koreait.product.controllers.RequestProduct;
import org.koreait.product.entities.Product;
import org.koreait.product.repositories.ProductRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
public class ProductUpdateService {
    private final ProductRepository repository;
    private final Utils utils;
    private final HttpServletRequest request;
    // private final ModelMapper mapper;   // 자동으로 entity에 매핑 -> mode가 없어서 못 씀

    /**
     * 상품 등록과 수정 process
     *
     * @param form 추가/수정 요청된 상품
     * @return 추가/수정된 상품 객체
     */
    public Product process(RequestProduct form) {
        String mode = form.getMode();
        Long seq = form.getSeq();   // 상품 등록번호

        System.out.println(form);

        Product item = seq == null || seq < 1L ? new Product() : repository.findById(seq).orElseGet(Product::new);

        if (mode == null || mode.equals("add")) {   // 상품 등록
            // 상품 등록 시에만 추가되는 정보
            item.setGid(form.getGid());
        }

        // 공통 저장 정보
        item.setName(form.getName());
        item.setCategory(form.getCategory());
        item.setStatus(form.getStatus());
        item.setConsumerPrice(form.getConsumerPrice());
        item.setSalePrice(form.getSalePrice());
        item.setDescription(form.getDescription());

        repository.save(item);

        return item;
    }

    public void processBatch(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("처리할 상품을 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        String method = request.getMethod();
        List<Product> products = new ArrayList<>();  // 수정할 상품 정보를 추가

        System.out.println(chks);

        for (int chk : chks) {
            Long seq = Long.valueOf(utils.getParam("seq_" + chk));
            Product product = repository.findById(seq).orElse(null);

            System.out.println("상품: " + product);
            if (product == null) continue;
            if (method.equalsIgnoreCase("DELETE")) {  // 상품 삭제 처리
                product.setDeletedAt(LocalDateTime.now());
                System.out.println("삭제");
            } else { // 수정처리
                // 상품 수정 일시 업데이트
//                boolean updateModifiedAt = Boolean.parseBoolean(Objects.requireNonNullElse(utils.getParam("updateModifiedAt_" + chk), "false"));
//                if (updateModifiedAt) {
//                    product.setModifiedAt(LocalDateTime.now());
//                }
                System.out.println("수정");
                // 상품 등록 해제 취소
                boolean cancelDelete = Boolean.parseBoolean(Objects.requireNonNullElse(utils.getParam("cancelDelete_" + chk), "false"));
                System.out.println("등록해제 " + cancelDelete);
                if (cancelDelete) {
                    product.setDeletedAt(null);
                }
            }

            products.add(product);
        }
        repository.saveAll(products);
    }
    /**
     * 전달받은 상품 ID 리스트에 대해 각각 상태를 변경합니다.
     *
     * @param ids       상태를 변경할 상품의 ID 리스트
     * @param statuses  각 상품에 적용할 새로운 상태값 리스트 (ids와 인덱스 일치)
     *
     */
    public void updateStatus(List<Long> ids, List<String> statuses) {
        if (ids == null || statuses == null || ids.size() != statuses.size()) return;

        List<Product> products = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            Product product = repository.findById(ids.get(i)).orElse(null);
            if (product == null) continue;

            try {
                ProductStatus ps = ProductStatus.valueOf(statuses.get(i));
                product.setStatus(ps);
                products.add(product);
            } catch (IllegalArgumentException ignore) {}
        }

        repository.saveAll(products);
    }
}
