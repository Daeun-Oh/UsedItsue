package org.koreait.product.services;

import lombok.RequiredArgsConstructor;
import org.koreait.product.entities.Product;
import org.koreait.product.repositories.ProductRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class ProductInfoService {

    private final ProductRepository repository;

    public List<Product> searchProducts(String productName, String category) {
        boolean hasName = productName != null && !productName.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasName && !hasCategory) {
            return repository.findAll();
        } else if (hasName && hasCategory) {
            return repository.findByProductNameAndCategory(productName, category);
        } else if (hasName) {
            return repository.findByProductName(productName);
        } else {
            return repository.findByCategory(category);
        }
    }
}
