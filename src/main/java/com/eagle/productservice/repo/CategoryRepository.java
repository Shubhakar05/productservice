package com.eagle.productservice.repo;

import com.eagle.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category ,Long> {

   Optional<Category> findByCategoryName(String categoryName);
    List<Category> findByParentCategoryName(Category parentCategory);



}
