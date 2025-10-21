package com.example.demo.controller.admin;

import com.example.demo.dto.CarrierDTO;
import com.example.demo.service.CarrierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/carriers")
public class AdminCarrierController {

    @Autowired
    private CarrierService carrierService;

    @GetMapping
    public String listCarriers(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Boolean active) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("carrierPage", carrierService.findCarriersByFilters(search, active, pageable));
        model.addAttribute("searchQuery", search);
        model.addAttribute("activeFilter", active);
        return "admin/carrier/list";
    }

    @GetMapping("/new")
    public String newCarrierForm(Model model) {
        model.addAttribute("carrierDTO", new CarrierDTO());
        return "admin/carrier/form";
    }

    @PostMapping
    public String createCarrier(@Valid @ModelAttribute("carrierDTO") CarrierDTO carrierDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/carrier/form";
        }
        carrierService.createCarrier(carrierDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo nhà vận chuyển thành công!");
        return "redirect:/admin/carriers";
    }

    @GetMapping("/{id}/edit")
    public String editCarrierForm(@PathVariable Integer id, Model model) {
        CarrierDTO carrierDTO = carrierService.getCarrierById(id);
        model.addAttribute("carrierDTO", carrierDTO);
        return "admin/carrier/form";
    }

    @PostMapping("/{id}")
    public String updateCarrier(@PathVariable Integer id,
                                @Valid @ModelAttribute("carrierDTO") CarrierDTO carrierDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/carrier/form";
        }
        carrierService.updateCarrier(id, carrierDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        return "redirect:/admin/carriers";
    }

    @GetMapping("/{id}/delete")
    public String deleteCarrier(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            carrierService.deleteCarrier(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/carriers";
    }
}