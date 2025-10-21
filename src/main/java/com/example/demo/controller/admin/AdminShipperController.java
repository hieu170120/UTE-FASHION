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
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Boolean active,
                               @RequestParam(required = false) Integer carrierId) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("shipperPage", shipperService.getAllShippersPagedWithFilters(pageable, search, active, carrierId));
        model.addAttribute("carriers", carrierService.getAllCarriers());
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("activeFilter", active);
        model.addAttribute("carrierIdFilter", carrierId);
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
                                @RequestParam(required = false) String username,
                                @RequestParam(required = false) String password,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("carriers", carrierService.getAllCarriers());
            return "admin/shipper/form";
        }
        
        try {
            shipperService.createShipperWithUser(shipperDTO, username, password);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo shipper và tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/shippers";
    }

    @GetMapping("/{id}/edit")
    public String editShipperForm(@PathVariable Integer id, Model model) {
        ShipperDTO shipperDTO = shipperService.getShipperById(id);
        model.addAttribute("shipperDTO", shipperDTO);
        model.addAttribute("carriers", carrierService.getAllCarriers());
        
        // Kiểm tra carrier có đang hoạt động không
        if (shipperDTO.getCarrierId() != null) {
            try {
                com.example.demo.dto.CarrierDTO carrier = carrierService.getCarrierById(shipperDTO.getCarrierId());
                model.addAttribute("carrierActive", carrier.isActive());
            } catch (Exception e) {
                model.addAttribute("carrierActive", false);
            }
        } else {
            model.addAttribute("carrierActive", true);
        }
        
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
    
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            shipperService.toggleShipperActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái shipper thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/shippers";
    }
}
