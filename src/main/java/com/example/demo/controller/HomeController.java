package com.example.demo.controller;

import com.example.demo.dto.ProductSummaryDTO;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@GetMapping("/")
	public String home(Model model) {
        List<ProductSummaryDTO> newestProducts = productService.getNewestProducts();
        model.addAttribute("newestProducts", newestProducts);

		List<ProductSummaryDTO> bestsellerProducts = productService.getBestsellerProducts();
		model.addAttribute("bestsellerProducts", bestsellerProducts);

		return "home";
	}
}
