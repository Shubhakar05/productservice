package com.eagle.productservice.controller;

import com.eagle.productservice.dto.ProductDto;
import com.eagle.productservice.dto.ProductRequestDTO;
import com.eagle.productservice.dto.ProductResponseDTO;
import com.eagle.productservice.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    // ✅ Removed @Autowired and used constructor-based injection
    private final ProductService productService;

    // ✅ Constructor injection promotes immutability and better testability
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //===========================================update product===========================================
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart("productDTO") ProductDto updatedProductDto
    ) throws IOException {
        ProductDto updated = productService.updateProduct(id, updatedProductDto, imageFile);
        return ResponseEntity.ok(updated);
    }

    //===========================================delete product===========================================
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully with ID: " + id);
    }

    //===========================================vimlesh - save product===========================================
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> saveProduct(
            @RequestPart("productDTO") ProductRequestDTO productDTO,
            @RequestPart("image") MultipartFile imageFile) throws IOException {

        ProductResponseDTO product = productService.createProduct(productDTO, imageFile);
        return ResponseEntity.ok(product);
    }

    //===========================================view single product by id===========================================
    @GetMapping("/view/by/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        ProductResponseDTO dto = productService.getProductById(id);
        return ResponseEntity.ok(dto);
    }

    //===========================================filter products===========================================
    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponseDTO>> filterProducts(
            @RequestParam String category,
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {

        List<ProductResponseDTO> products = productService.getProductsByCategoryTreeAndPrice(category, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    //===========================================view all products===========================================
    @GetMapping("/view")
    public ResponseEntity<List<ProductResponseDTO>> viewAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
