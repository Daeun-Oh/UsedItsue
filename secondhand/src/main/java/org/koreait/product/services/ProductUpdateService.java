package org.koreait.product.services;

import lombok.RequiredArgsConstructor;
import org.koreait.product.controllers.RequestProduct;
import org.koreait.product.entities.Product;
import org.koreait.product.repositories.ProductRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class ProductUpdateService {
    private final ProductRepository repository;
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
    
}
