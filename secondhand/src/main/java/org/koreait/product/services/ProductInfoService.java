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

    public List<Product> searchProducts(String name, String category) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasName && !hasCategory) {
            return repository.findAll();
        } else if (hasName && hasCategory) {
            return repository.findByNameAndCategory(name, category);
        } else if (hasName) {
            return repository.findByName(name);
        } else {
            return repository.findByCategory(category);
        }
    }
}
