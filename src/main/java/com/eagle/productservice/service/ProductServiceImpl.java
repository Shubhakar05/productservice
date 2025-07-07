package com.eagle.productservice.service;

import com.eagle.productservice.awsservice.S3Service;
import com.eagle.productservice.dto.ProductDto;
import com.eagle.productservice.dto.ProductRequestDTO;
import com.eagle.productservice.dto.ProductResponseDTO;
import com.eagle.productservice.entity.Category;
import com.eagle.productservice.entity.Product;
import com.eagle.productservice.repo.CategoryRepository;
import com.eagle.productservice.repo.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              S3Service s3Service,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.s3Service = s3Service;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productDTO, MultipartFile imageFile) {
        String imageUrl;
        try {
            imageUrl = s3Service.uploadFile(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }

        final Category parentCategory = (productDTO.getParentCategoryName() != null &&
                !productDTO.getParentCategoryName().isBlank())
                ? categoryRepository.findByCategoryName(productDTO.getParentCategoryName())
                .orElseGet(() -> {
                    Category newParent = new Category();
                    newParent.setCategoryName(productDTO.getParentCategoryName());
                    return categoryRepository.save(newParent);
                })
                : null;

        Category category = categoryRepository.findByCategoryName(productDTO.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setCategoryName(productDTO.getCategoryName());
                    newCategory.setParentCategoryName(parentCategory);
                    return categoryRepository.save(newCategory);
                });

        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setGstPercentage(productDTO.getGstPercentage());
        product.setHsnCode(productDTO.getHsnCode());
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setStockQuantity(productDTO.getStockQuantity());

        Product savedProduct = productRepository.save(product);

        ProductResponseDTO responseDTO = new ProductResponseDTO();
        BeanUtils.copyProperties(savedProduct, responseDTO);
        responseDTO.setCategoryName(category.getCategoryName());
        if (category.getParentCategoryName() != null) {
            responseDTO.setParentCategoryName(category.getParentCategoryName().getCategoryName());
        }

        return responseDTO;
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto updatedProductDto, MultipartFile imageFile) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        existingProduct.setProductName(updatedProductDto.getProductName());
        existingProduct.setDescription(updatedProductDto.getDescription());
        existingProduct.setPrice(updatedProductDto.getPrice());
        existingProduct.setGstPercentage(updatedProductDto.getGstPercentage());
        existingProduct.setHsnCode(updatedProductDto.getHsnCode());
        existingProduct.setImageUrl(updatedProductDto.getImageUrl());
        existingProduct.setIsActive(updatedProductDto.getIsActive());
        existingProduct.setStockQuantity(updatedProductDto.getStockQuantity());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String newImageUrl = s3Service.uploadFile(imageFile);
                existingProduct.setImageUrl(newImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        if (updatedProductDto.getCategoryId() != null) {
            Category category = new Category();
            category.setCategory_id(updatedProductDto.getCategoryId());
            existingProduct.setCategory(category);
        }

        Product updated = productRepository.save(existingProduct);
        return convertToProductDto(updated);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToProductResponseDto(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategoryTreeAndPrice(String categoryName, double minPrice, double maxPrice) {
        Category parent = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Category> allCategories = new ArrayList<>();
        collectAllSubCategories(parent, allCategories);

        List<Product> productList = productRepository.findByCategoryInAndPriceBetween(allCategories, minPrice, maxPrice);

        return productList.stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    private void collectAllSubCategories(Category parent, List<Category> all) {
        all.add(parent);
        List<Category> children = categoryRepository.findByParentCategoryName(parent);
        for (Category child : children) {
            collectAllSubCategories(child, all);
        }
    }

    private ProductDto convertToProductDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProdId(product.getProd_id());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setGstPercentage(product.getGstPercentage());
        dto.setHsnCode(product.getHsnCode());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setIsActive(product.getIsActive());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategory_id());
        }
        return dto;
    }

    private ProductResponseDTO convertToProductResponseDto(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setGstPercentage(product.getGstPercentage());
        dto.setHsnCode(product.getHsnCode());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setIsActive(product.getIsActive());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getCategoryName());
            if (product.getCategory().getParentCategoryName() != null) {
                dto.setParentCategoryName(product.getCategory().getParentCategoryName().getCategoryName());
            }
        }
        return dto;
    }
}
