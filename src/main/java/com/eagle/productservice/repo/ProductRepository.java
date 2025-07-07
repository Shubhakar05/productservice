package com.eagle.productservice.repo;

import com.eagle.productservice.entity.Category;
import com.eagle.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByCategoryInAndPriceBetween(List<Category> categories, double minPrice, double maxPrice);

}
