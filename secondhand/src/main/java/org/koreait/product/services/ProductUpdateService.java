package org.koreait.product.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.script.AlertException;
import org.koreait.global.libs.Utils;
import org.koreait.member.entities.Member;
import org.koreait.member.repositories.MemberRepository;
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
     * @param form : 추가/수정 요청된 상품
     * @return : 추가/수정된 상품 객체
     */
    public Product process(RequestProduct form) {
        String mode = form.getMode();
        Long seq = form.getSeq();   // 상품 등록번호

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

        for (int chk : chks) {
            Long seq = Long.valueOf(utils.getParam("seq_" + chk));
            Product product = repository.findById(seq).orElse(null);
            if (product == null) continue;
            if (method.equalsIgnoreCase("DELETE")) {  // 상품 삭제 처리
                product.setDeletedAt(LocalDateTime.now());
            } /*else { // 수정처리
                // 비밀번호 변경일시 업데이트
                boolean updateCredentialAt = Boolean.parseBoolean(Objects.requireNonNullElse(utils.getParam("updateCredentialAt_" + chk), "false"));
                if (updateCredentialAt) {
                    member.setCredentialChangedAt(LocalDateTime.now());
                }
            }*/

            products.add(product);
        }
        repository.saveAll(products);
    }
}
