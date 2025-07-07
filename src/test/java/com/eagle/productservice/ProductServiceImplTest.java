package com.eagle.productservice;

import com.eagle.productservice.awsservice.S3Service;
import com.eagle.productservice.dto.ProductRequestDTO;
import com.eagle.productservice.dto.ProductResponseDTO;
import com.eagle.productservice.entity.Category;
import com.eagle.productservice.entity.Product;
import com.eagle.productservice.repo.CategoryRepository;
import com.eagle.productservice.repo.ProductRepository;
import com.eagle.productservice.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private S3Service s3Service;

    private ProductRequestDTO requestDTO;
    private MultipartFile imageFile;
    private Category category;
    private Category parentCategory;
    private Product product;

    @BeforeEach
    void setUp() {
        requestDTO = new ProductRequestDTO();
        requestDTO.setProductName("Nike Shoes");
        requestDTO.setDescription("Latest model");
        requestDTO.setPrice(9000.0);
        requestDTO.setGstPercentage(18.0);
        requestDTO.setHsnCode("8516");
        requestDTO.setCategoryName("Shoes");
        requestDTO.setParentCategoryName("Footwear");

        imageFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", "fake-image-content".getBytes());

        parentCategory = new Category();
        parentCategory.setCategoryName("Footwear");

        category = new Category();
        category.setCategoryName("Shoes");
        category.setParentCategoryName(parentCategory);

        product = new Product();
        product.setProductName("Nike Shoes");
        product.setDescription("Latest model");
        product.setPrice(9000.0);
        product.setGstPercentage(18.0);
        product.setHsnCode("8516");
        product.setImageUrl("http://image-url");
        product.setCategory(category);
    }

    @Test
    void testCreateProduct_Success() throws IOException {
        when(s3Service.uploadFile(imageFile)).thenReturn("http://image-url");
        when(categoryRepository.findByCategoryName("Footwear")).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findByCategoryName("Shoes")).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO response = productService.createProduct(requestDTO, imageFile);

        assertNotNull(response);
        assertEquals("Nike Shoes", response.getProductName());
        assertEquals("Shoes", response.getCategoryName());
        assertEquals("Footwear", response.getParentCategoryName());
        assertEquals("http://image-url", response.getImageUrl());

        verify(s3Service, times(1)).uploadFile(imageFile);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_ImageUploadFails_ThrowsException() throws IOException {
        when(s3Service.uploadFile(imageFile)).thenThrow(new IOException("Upload error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(requestDTO, imageFile);
        });

        assertTrue(ex.getMessage().contains("Failed to upload image"));
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO dto = productService.getProductById(1L);

        assertEquals("Nike Shoes", dto.getProductName());
        assertEquals("Shoes", dto.getCategoryName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
    }

    @Test
    void testGetAllProducts_ReturnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> dtos = productService.getAllProducts();

        assertEquals(1, dtos.size());
        assertEquals("Nike Shoes", dtos.get(0).getProductName());
    }

    @Test
    void testGetProductsByCategoryTreeAndPrice_Success() {
        when(categoryRepository.findByCategoryName("Footwear")).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCategoryName(category)).thenReturn(List.of());
        when(productRepository.findByCategoryInAndPriceBetween(anyList(), eq(5000.0), eq(10000.0)))
                .thenReturn(List.of(product));

        List<ProductResponseDTO> result = productService.getProductsByCategoryTreeAndPrice("Footwear", 5000.0, 10000.0);

        assertEquals(1, result.size());
        assertEquals("Nike Shoes", result.get(0).getProductName());
    }
}
