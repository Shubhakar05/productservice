package com.eagle.productservice.dto;


import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProductRequestDTO {

    private String productName;

    @Column(length = 1000)
    private String description;

    private double price;

    @Column(name = "gst_percentage")
    private double gstPercentage;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(length = 30)
    private String categoryName;

    private String parentCategoryName;
}
