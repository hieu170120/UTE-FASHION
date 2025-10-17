package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreationRequest {

    @NotNull(message = "Product ID cannot be null")
    private Integer productId;

    // Optional: Only required if reviewing from order
    private Integer orderId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    private String title;

    @Size(min = 50, message = "Comment must be at least 50 characters long")
    private String comment;

    private List<String> imageUrls;

    private List<String> videoUrls;
}
