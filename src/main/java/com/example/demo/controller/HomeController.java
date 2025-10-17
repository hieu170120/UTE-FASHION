package com.example.demo.controller;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@GetMapping("/")
	public String home(Model model) {
        CompletableFuture<List<ProductSummaryDTO>> newestProductsFuture = productService.getNewestProducts();
        CompletableFuture<List<ProductSummaryDTO>> bestsellerProductsFuture = productService.getBestsellerProducts();

        CompletableFuture.allOf(newestProductsFuture, bestsellerProductsFuture).join();

        try {
            model.addAttribute("newestProducts", newestProductsFuture.get());
            model.addAttribute("bestsellerProducts", bestsellerProductsFuture.get());
        } catch (Exception e) {
            // Handle exceptions, e.g., log them or add an error message to the model
            model.addAttribute("errorMessage", "Could not load products. Please try again later.");
        }

		return "home";
	}
}
