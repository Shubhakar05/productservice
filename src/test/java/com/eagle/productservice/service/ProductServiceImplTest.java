package com.eagle.productservice.service;

import com.eagle.productservice.awsservice.S3Service;
import com.eagle.productservice.dto.ProductDto;
import com.eagle.productservice.entity.Category;
import com.eagle.productservice.entity.Product;
import com.eagle.productservice.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    private Product existingProduct;
    private ProductDto updatedDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingProduct = new Product();
        existingProduct.setProd_id(1L);
        existingProduct.setProductName("Old Product");
        existingProduct.setDescription("Old Desc");
        existingProduct.setPrice(500);
        existingProduct.setGstPercentage(18);
        existingProduct.setHsnCode("1234");
        existingProduct.setImageUrl("old.jpg");
        existingProduct.setCreatedAt(LocalDateTime.now());
        existingProduct.setIsActive(true);
        existingProduct.setStockQuantity(10);

        Category category = new Category();
        category.setCategory_id(2L);
        category.setCategoryName("Electronics");
        existingProduct.setCategory(category);

        updatedDto = new ProductDto();
        updatedDto.setProdId(1L);
        updatedDto.setProductName("Updated Product");
        updatedDto.setDescription("Updated Desc");
        updatedDto.setPrice(1000);
        updatedDto.setGstPercentage(12);
        updatedDto.setHsnCode("5678");
        updatedDto.setImageUrl("new.jpg");
        updatedDto.setCreatedAt(LocalDateTime.now());
        updatedDto.setIsActive(false);
        updatedDto.setStockQuantity(5);
        updatedDto.setCategoryId(2L);
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.isEmpty()).thenReturn(false);
        when(mockImage.getOriginalFilename()).thenReturn("new.jpg");
        when(s3Service.uploadFile(mockImage)).thenReturn("https://s3.aws.com/new.jpg");

        Product updatedProduct = new Product();
        updatedProduct.setProd_id(1L);
        updatedProduct.setProductName("Updated Product");
        updatedProduct.setDescription("Updated Desc");
        updatedProduct.setPrice(1000);
        updatedProduct.setGstPercentage(12);
        updatedProduct.setHsnCode("5678");
        updatedProduct.setImageUrl("https://s3.aws.com/new.jpg");
        updatedProduct.setCreatedAt(LocalDateTime.now());
        updatedProduct.setIsActive(false);
        updatedProduct.setStockQuantity(5);

        Category category = new Category();
        category.setCategory_id(2L);
        category.setCategoryName("Electronics");
        updatedProduct.setCategory(category);

        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDto result = productService.updateProduct(1L, updatedDto, mockImage);

        assertNotNull(result);
        assertEquals("Updated Product", result.getProductName());
        assertEquals("https://s3.aws.com/new.jpg", result.getImageUrl());
        verify(productRepository).save(any(Product.class));
        verify(s3Service).uploadFile(mockImage);
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(99L, updatedDto, null));

        assertEquals("Product not found with ID: 99", ex.getMessage());
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void testUpdateProduct_WithNullCategoryId() {
        updatedDto.setCategoryId(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductDto result = productService.updateProduct(1L, updatedDto, null);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(99L));

        assertEquals("Product not found with ID: 99", ex.getMessage());
    }
}
