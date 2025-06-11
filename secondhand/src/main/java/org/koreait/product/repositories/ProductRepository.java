package org.koreait.product.repositories;

import org.koreait.product.entities.Product;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProductRepository extends ListCrudRepository<Product, Long> {
    List<Product> findByProductName(String productName);
    List<Product> findByCategory(String category);
    List<Product> findByProductNameAndCategory(String productName, String category);
}
