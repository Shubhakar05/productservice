package com.eagle.productservice.service;

import com.eagle.productservice.dto.ProductRequestDTO;
import com.eagle.productservice.dto.ProductResponseDTO;
import com.eagle.productservice.entity.Product;
import org.springframework.web.multipart.MultipartFile;
import com.eagle.productservice.dto.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productDTO, MultipartFile imageFile);
    ProductResponseDTO getProductById(Long id);
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> getProductsByCategoryTreeAndPrice(String categoryName, double minPrice, double maxPrice);


    ProductDto updateProduct(Long productId, ProductDto updatedProductDto,MultipartFile imageFile);
    void deleteProduct(Long id);
}
