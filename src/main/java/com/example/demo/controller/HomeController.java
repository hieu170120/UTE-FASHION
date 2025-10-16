package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;

@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@GetMapping("/")
	public String home(Model model) {
        List<ProductDTO> newestProducts = productService.getNewestProducts();
        model.addAttribute("newestProducts", newestProducts);

		List<ProductDTO> bestsellerProducts = productService.getBestsellerProducts();
		model.addAttribute("bestsellerProducts", bestsellerProducts);

		return "home";
	}
}
