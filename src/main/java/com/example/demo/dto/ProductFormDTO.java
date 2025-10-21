package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProductFormDTO {

    private ProductDTO product = new ProductDTO();
    
    private List<ProductImageDTO> images = new ArrayList<>();
    
    private Integer primaryImageIndex;

    public ProductFormDTO() {
        this.images.add(new ProductImageDTO());
        this.images.add(new ProductImageDTO());
        this.images.add(new ProductImageDTO());
    }
}
