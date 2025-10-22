package com.example.demo.service;

import com.example.demo.dto.RevenueReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class ExcelExportService {
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    public byte[] exportRevenueReport(RevenueReportDTO report, String startDate, String endDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);
            
            // Sheet 1: Tổng quan
            Sheet overviewSheet = workbook.createSheet("Tổng Quan");
            createOverviewSheet(overviewSheet, report, startDate, endDate, headerStyle, currencyStyle, percentStyle, normalStyle);
            
            // Sheet 2: Doanh thu theo ngày
            Sheet dailySheet = workbook.createSheet("Doanh Thu Theo Ngày");
            createDailyRevenueSheet(dailySheet, report, headerStyle, currencyStyle, normalStyle);
            
            // Sheet 3: Danh mục bán chạy
            Sheet categorySheet = workbook.createSheet("Danh Mục Bán Chạy");
            createCategorySheet(categorySheet, report, headerStyle, currencyStyle, normalStyle);
            
            // Sheet 4: Tỷ lệ chuyển đổi
            Sheet conversionSheet = workbook.createSheet("Tỷ Lệ Chuyển Đổi");
            createConversionSheet(conversionSheet, report, headerStyle, percentStyle, normalStyle);
            
            // Sheet 5: Đơn hàng gần đây
            Sheet ordersSheet = workbook.createSheet("Đơn Hàng Gần Đây");
            createRecentOrdersSheet(ordersSheet, report, headerStyle, currencyStyle, normalStyle);
            
            // Auto-size columns
            for (Sheet sheet : workbook) {
                for (int i = 0; i < 10; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private void createOverviewSheet(Sheet sheet, RevenueReportDTO report, String startDate, String endDate,
                                    CellStyle headerStyle, CellStyle currencyStyle, CellStyle percentStyle, CellStyle normalStyle) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");
        titleCell.setCellStyle(headerStyle);
        
        // Date range
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Thời gian: " + startDate + " - " + endDate);
        rowNum++;
        
        // Overview stats
        createDataRow(sheet, rowNum++, "Tổng Doanh Thu", report.getTotalRevenue(), currencyStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng", report.getRevenueGrowthPercent(), percentStyle);
        rowNum++;
        
        createDataRow(sheet, rowNum++, "Tổng Đơn Hàng", BigDecimal.valueOf(report.getTotalOrders()), normalStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng", report.getOrdersGrowthPercent(), percentStyle);
        rowNum++;
        
        createDataRow(sheet, rowNum++, "Lượt Xem", BigDecimal.valueOf(report.getTotalViews()), normalStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng", report.getViewsGrowthPercent(), percentStyle);
        rowNum++;
        
        // Monthly target
        Row targetHeaderRow = sheet.createRow(rowNum++);
        targetHeaderRow.createCell(0).setCellValue("MỤC TIÊU THÁNG");
        targetHeaderRow.getCell(0).setCellStyle(headerStyle);
        
        createDataRow(sheet, rowNum++, "Mục tiêu", report.getMonthlyTarget(), currencyStyle);
        createDataRow(sheet, rowNum++, "Đạt được", report.getCurrentMonthRevenue(), currencyStyle);
        createDataRow(sheet, rowNum++, "Tiến độ", report.getTargetProgress(), percentStyle);
    }
    
    private void createDailyRevenueSheet(Sheet sheet, RevenueReportDTO report, 
                                        CellStyle headerStyle, CellStyle currencyStyle, CellStyle normalStyle) {
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Ngày", "Doanh Thu", "Số Đơn Hàng"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (RevenueReportDTO.DailyRevenueDTO daily : report.getDailyRevenue()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(daily.getDateLabel());
            
            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(daily.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            
            Cell orderCell = row.createCell(2);
            orderCell.setCellValue(daily.getOrderCount());
            orderCell.setCellStyle(normalStyle);
        }
    }
    
    private void createCategorySheet(Sheet sheet, RevenueReportDTO report,
                                    CellStyle headerStyle, CellStyle currencyStyle, CellStyle normalStyle) {
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Danh Mục", "Doanh Thu", "Số Đơn"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (RevenueReportDTO.CategorySalesDTO category : report.getTopCategories()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category.getCategoryName());
            
            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(category.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            
            Cell orderCell = row.createCell(2);
            orderCell.setCellValue(category.getOrderCount());
            orderCell.setCellStyle(normalStyle);
        }
    }
    
    private void createConversionSheet(Sheet sheet, RevenueReportDTO report,
                                      CellStyle headerStyle, CellStyle percentStyle, CellStyle normalStyle) {
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Giai Đoạn", "Số Lượng", "Tỷ Lệ (%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        RevenueReportDTO.ConversionFunnelDTO funnel = report.getConversionFunnel();
        
        createConversionRow(sheet, rowNum++, "Xem Sản Phẩm", funnel.getViewCount(), 100.0, normalStyle, percentStyle);
        createConversionRow(sheet, rowNum++, "Thêm Vào Giỏ", funnel.getAddToCartCount(), 
            funnel.getViewCount() > 0 ? (funnel.getAddToCartCount() * 100.0 / funnel.getViewCount()) : 0, 
            normalStyle, percentStyle);
        createConversionRow(sheet, rowNum++, "Thanh Toán", funnel.getCheckoutCount(),
            funnel.getViewCount() > 0 ? (funnel.getCheckoutCount() * 100.0 / funnel.getViewCount()) : 0,
            normalStyle, percentStyle);
        createConversionRow(sheet, rowNum++, "Hoàn Thành", funnel.getCompletedCount(),
            funnel.getViewCount() > 0 ? (funnel.getCompletedCount() * 100.0 / funnel.getViewCount()) : 0,
            normalStyle, percentStyle);
    }
    
    private void createRecentOrdersSheet(Sheet sheet, RevenueReportDTO report,
                                        CellStyle headerStyle, CellStyle currencyStyle, CellStyle normalStyle) {
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Mã ĐH", "Khách Hàng", "Sản Phẩm", "Số Lượng", "Tổng Tiền", "Trạng Thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (RevenueReportDTO.RecentOrderDTO order : report.getRecentOrders()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("#" + order.getOrderNumber());
            row.createCell(1).setCellValue(order.getCustomerName());
            row.createCell(2).setCellValue(order.getProductName());
            
            Cell qtyCell = row.createCell(3);
            qtyCell.setCellValue(order.getQuantity());
            qtyCell.setCellStyle(normalStyle);
            
            Cell totalCell = row.createCell(4);
            totalCell.setCellValue(order.getTotalAmount().doubleValue());
            totalCell.setCellStyle(currencyStyle);
            
            row.createCell(5).setCellValue(order.getStatus());
        }
    }
    
    private void createDataRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }
    
    private void createConversionRow(Sheet sheet, int rowNum, String stage, int count, double percent,
                                    CellStyle countStyle, CellStyle percentStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(stage);
        
        Cell countCell = row.createCell(1);
        countCell.setCellValue(count);
        countCell.setCellStyle(countStyle);
        
        Cell percentCell = row.createCell(2);
        percentCell.setCellValue(percent / 100);
        percentCell.setCellStyle(percentStyle);
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"₫\""));
        return style;
    }
    
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
        return style;
    }
    
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }
}
