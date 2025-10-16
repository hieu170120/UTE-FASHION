package com.example.demo.controller.admin;

import com.example.demo.dto.ShipperDTO;
import com.example.demo.service.CarrierService;
import com.example.demo.service.ShipperService;
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
@RequestMapping("/admin/shippers")
public class AdminShipperController {

    @Autowired
    private ShipperService shipperService;

    @Autowired
    private CarrierService carrierService;

    @GetMapping
    public String listShippers(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("shipperPage", shipperService.getAllShippersPaged(pageable));
        return "admin/shipper/list";
    }

    @GetMapping("/new")
    public String newShipperForm(Model model) {
        model.addAttribute("shipperDTO", new ShipperDTO());
        model.addAttribute("carriers", carrierService.getAllCarriers());
        return "admin/shipper/form";
    }

    @PostMapping
    public String createShipper(@Valid @ModelAttribute("shipperDTO") ShipperDTO shipperDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("carriers", carrierService.getAllCarriers());
            return "admin/shipper/form";
        }
        shipperService.createShipper(shipperDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo shipper thành công!");
        return "redirect:/admin/shippers";
    }

    @GetMapping("/{id}/edit")
    public String editShipperForm(@PathVariable Integer id, Model model) {
        ShipperDTO shipperDTO = shipperService.getShipperById(id);
        model.addAttribute("shipperDTO", shipperDTO);
        model.addAttribute("carriers", carrierService.getAllCarriers());
        return "admin/shipper/form";
    }

    @PostMapping("/{id}")
    public String updateShipper(@PathVariable Integer id,
                                @Valid @ModelAttribute("shipperDTO") ShipperDTO shipperDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("carriers", carrierService.getAllCarriers());
            return "admin/shipper/form";
        }
        shipperService.updateShipper(id, shipperDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật shipper thành công!");
        return "redirect:/admin/shippers";
    }

    @GetMapping("/{id}/delete")
    public String deleteShipper(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shipperService.deleteShipper(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa shipper thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/shippers";
    }
}