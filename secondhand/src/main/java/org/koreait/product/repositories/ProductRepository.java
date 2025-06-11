package org.koreait.product.repositories;

import org.koreait.product.constants.ProductStatus;
import org.koreait.product.entities.Product;
import org.koreait.trend.entities.Trend;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductRepository extends ListCrudRepository<Product, Long> {

}
