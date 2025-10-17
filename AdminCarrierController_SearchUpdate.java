// UPDATED: AdminCarrierController.java - Thêm chức năng tìm kiếm

@Controller
@RequestMapping("/admin/carriers")
public class AdminCarrierController {

    @Autowired
    private CarrierService carrierService;

    /**
     * Danh sách carriers với tìm kiếm và phân trang
     */
    @GetMapping
    public String listCarriers(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CarrierDTO> carrierPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm theo tên hoặc mô tả
            carrierPage = carrierService.searchCarriers(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            // Hiển thị tất cả
            carrierPage = carrierService.getAllCarriersPaged(pageable);
        }
        
        model.addAttribute("carrierPage", carrierPage);
        model.addAttribute("carriers", carrierPage.getContent()); // Để tương thích với template hiện tại
        
        return "admin/carrier/list";
    }

    // ... các method khác giữ nguyên ...
}