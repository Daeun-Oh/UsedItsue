package org.koreait.product.repositories;

import org.koreait.product.entities.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductRepository extends ListCrudRepository<Product, Long> {

}
