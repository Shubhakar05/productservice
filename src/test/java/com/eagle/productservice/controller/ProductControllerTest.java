package com.eagle.productservice.controller;

import com.eagle.productservice.dto.ProductDto;
import com.eagle.productservice.dto.ProductRequestDTO;
import com.eagle.productservice.dto.ProductResponseDTO;
import com.eagle.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        sampleProductDto = new ProductDto();
        sampleProductDto.setProdId(1L);
        sampleProductDto.setProductName("Test Product");
        sampleProductDto.setDescription("Test Desc");
        sampleProductDto.setPrice(999.0);
        sampleProductDto.setGstPercentage(18);
        sampleProductDto.setHsnCode("1234");
        sampleProductDto.setImageUrl("test.jpg");
        sampleProductDto.setIsActive(true);
        sampleProductDto.setStockQuantity(50);
        sampleProductDto.setCategoryId(2L);
    }

    @Test
    void testUpdateProductWithImage() throws Exception {
        ProductDto updatedDto = new ProductDto();
        updatedDto.setProdId(1L);
        updatedDto.setProductName("Updated Product");
        updatedDto.setDescription("Updated Desc");
        updatedDto.setPrice(1999.0);
        updatedDto.setGstPercentage(12);
        updatedDto.setHsnCode("5678");
        updatedDto.setImageUrl("http://updated-image.jpg");
        updatedDto.setIsActive(false);
        updatedDto.setStockQuantity(25);
        updatedDto.setCategoryId(3L);

        MockMultipartFile productDTO = new MockMultipartFile(
                "productDTO", "", "application/json", new ObjectMapper().writeValueAsBytes(updatedDto));

        MockMultipartFile image = new MockMultipartFile(
                "image", "updated.jpg", "image/jpeg", "image-bytes".getBytes());

        when(productService.updateProduct(eq(1L), any(ProductDto.class), any(MultipartFile.class))).thenReturn(updatedDto);

        mockMvc.perform(multipart("/api/product/update/1")
                        .file(productDTO)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated Desc"));
    }

    @Test
    void testUpdateProductWithoutImage() throws Exception {
        ProductDto updatedDto = new ProductDto();
        updatedDto.setProdId(1L);
        updatedDto.setProductName("Updated Without Image");
        updatedDto.setDescription("No image provided");
        updatedDto.setPrice(1500.0);
        updatedDto.setCategoryId(2L);

        MockMultipartFile productDTO = new MockMultipartFile(
                "productDTO", "", "application/json", new ObjectMapper().writeValueAsBytes(updatedDto));

        when(productService.updateProduct(eq(1L), any(ProductDto.class), isNull())).thenReturn(updatedDto);

        mockMvc.perform(multipart("/api/product/update/1")
                        .file(productDTO)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Without Image"));
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/product/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully with ID: 1"));
    }

    @Test
    void testSaveProduct() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setProductName("Nike Shoes");
        requestDTO.setDescription("Latest");
        requestDTO.setPrice(9000.0);
        requestDTO.setHsnCode("8516");
        requestDTO.setGstPercentage(18.0);
        requestDTO.setCategoryName("Shoes");
        requestDTO.setParentCategoryName("Footwear");

        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setProductName("Nike Shoes");
        responseDTO.setCategoryName("Shoes");
        responseDTO.setParentCategoryName("Footwear");
        responseDTO.setImageUrl("http://image-url");

        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "fake".getBytes());
        MockMultipartFile productDTO = new MockMultipartFile("productDTO", "", "application/json",
                new ObjectMapper().writeValueAsBytes(requestDTO));

        when(productService.createProduct(any(), any())).thenReturn(responseDTO);

        mockMvc.perform(multipart("/api/product/add")
                        .file(productDTO)
                        .file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Nike Shoes"))
                .andExpect(jsonPath("$.categoryName").value("Shoes"));
    }

    @Test
    void testGetProductById() throws Exception {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductName("Nike Shoes");
        dto.setCategoryName("Shoes");

        when(productService.getProductById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/product/view/by/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Nike Shoes"))
                .andExpect(jsonPath("$.categoryName").value("Shoes"));
    }

    @Test
    void testFilterProducts() throws Exception {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductName("Nike Shoes");

        when(productService.getProductsByCategoryTreeAndPrice("Shoes", 5000.0, 10000.0))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/product/filter")
                        .param("category", "Shoes")
                        .param("minPrice", "5000")
                        .param("maxPrice", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Nike Shoes"));
    }

    @Test
    void testViewAllProducts() throws Exception {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductName("Nike Shoes");

        when(productService.getAllProducts()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/product/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Nike Shoes"));
    }
}
