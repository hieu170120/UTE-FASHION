use ute_fashion;
go

use ute_fashion;
go

SET NOCOUNT ON;

-- URL ảnh bạn cung cấp
DECLARE @ImageURL NVARCHAR(500) = 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=688';
DECLARE @VendorID INT = 1; -- GIẢ ĐỊNH VENDOR ID LÀ 1

-- KHAI BÁO CÁC BIẾN CHUNG (Chỉ khai báo, không dùng để fetch trong cursor)
DECLARE @BasePrice DECIMAL(18,2);
DECLARE @SalePrice DECIMAL(18,2);
DECLARE @CostPrice DECIMAL(18,2);
DECLARE @Stock INT;
DECLARE @ProductName NVARCHAR(255);
DECLARE @Slug NVARCHAR(255);
DECLARE @SKU_Prefix NVARCHAR(10);
DECLARE @CategoryID INT;
DECLARE @BrandID INT;
DECLARE @ProductID INT;
DECLARE @ProductType NVARCHAR(50);
DECLARE @ShortDesc NVARCHAR(500);

-- =============================================
-- INSERT DỮ LIỆU CỐ ĐỊNH CHO CÁC BẢNG (REFERENCE DATA)
-- (Phần này chạy bình thường)
-- =============================================
-- SHOPS (Cửa hàng)
INSERT INTO Shops (vendor_id, shop_name, slug, description, logo_url, is_active)
VALUES 
(1, N'Shop Thời Trang Ecom', 'shop-thoi-trang-ecom', N'Cung cấp đa dạng sản phẩm thời trang, chất lượng hàng đầu.', @ImageURL, 1),
(1, N'Cửa Hàng Phụ Kiện Cao Cấp', 'cua-hang-phu-kien-cao-cap', N'Phụ kiện độc đáo, sang trọng.', @ImageURL, 1);

-- CATEGORIES (Danh mục)
INSERT INTO Categories (category_name, slug, description, parent_category_id, image_url)
VALUES 
(N'Thời Trang Nam', 'thoi-trang-nam', N'Các sản phẩm thời trang dành cho nam giới.', NULL, @ImageURL), -- ID: 1
(N'Thời Trang Nữ', 'thoi-trang-nu', N'Các sản phẩm thời trang dành cho nữ giới.', NULL, @ImageURL), -- ID: 2
(N'Áo Thun & Polo', 'ao-thun-polo', N'Đa dạng các loại áo thun và polo.', 1, @ImageURL), -- ID: 3
(N'Quần Dài', 'quan-dai', N'Quần jean, kaki, tây các loại.', 1, @ImageURL), -- ID: 4
(N'Váy & Đầm', 'vay-dam', N'Váy đầm công sở, dạo phố.', 2, @ImageURL), -- ID: 5
(N'Phụ Kiện', 'phu-kien', N'Túi xách, ví da, trang sức.', NULL, @ImageURL); -- ID: 6

-- BRANDS (Thương hiệu)
INSERT INTO Brands (brand_name, slug, description, logo_url, website_url)
VALUES 
(N'Brand Elegant', 'brand-elegant', N'Thương hiệu chuyên đồ công sở.', @ImageURL, 'https://elegant.com'), -- ID: 1
(N'Street Vibes', 'street-vibes', N'Thương hiệu phong cách đường phố.', @ImageURL, 'https://streetvibes.com'), -- ID: 2
(N'Sport Pro', 'sport-pro', N'Thương hiệu đồ thể thao.', @ImageURL, 'https://sportpro.com'), -- ID: 3
(N'Luxury Acc', 'luxury-acc', N'Thương hiệu phụ kiện xa xỉ.', @ImageURL, 'https://luxuryacc.com'); -- ID: 4

-- SIZES (Kích cỡ)
INSERT INTO Sizes (size_name, size_type, display_order)
VALUES 
('S', 'Clothing', 1), -- ID: 1
('M', 'Clothing', 2), -- ID: 2
('L', 'Clothing', 3), -- ID: 3
('XL', 'Clothing', 4), -- ID: 4
('Freesize', 'General', 5); -- ID: 5

-- COLORS (Màu sắc)
INSERT INTO Colors (color_name, color_code)
VALUES 
(N'Đen', '#000000'), -- ID: 1
(N'Trắng', '#FFFFFF'), -- ID: 2
(N'Xanh Navy', '#000080'), -- ID: 3
(N'Be', '#F5F5DC'), -- ID: 4
(N'Hồng Pastel', '#F8C8DC'); -- ID: 5

---

DECLARE @Count INT = 1;
-- Các mảng dữ liệu mô phỏng
DECLARE @ProductNames TABLE (ID INT IDENTITY(1,1), Name NVARCHAR(100), Category INT, SKU_Pre NVARCHAR(10), Type NVARCHAR(50));
INSERT INTO @ProductNames (Name, Category, SKU_Pre, Type) VALUES 
(N'Áo Thun Cotton In Hình', 3, 'ATC', N'Thun'), 
(N'Quần Tây Form Slimfit', 4, 'QTS', N'Quần'), 
(N'Váy Xòe Công Sở', 5, 'VXS', N'Váy'), 
(N'Túi Xách Da Cao Cấp', 6, 'TXD', N'Phụ Kiện'),
(N'Áo Polo Cổ Bẻ Thêu Logo', 3, 'APC', N'Polo'),
(N'Đầm Suông Cổ Tròn', 5, 'DSC', N'Đầm');
DECLARE @NameCount INT = (SELECT COUNT(*) FROM @ProductNames);

WHILE @Count <= 100
BEGIN
    -- KHAI BÁO BIẾN CHO PHẠM VI LOOP (ĐÃ SỬA LỖI MSG 137)
    DECLARE @SizeID INT;
    DECLARE @ColorID INT;
    
    -- Chọn mẫu sản phẩm ngẫu nhiên
    DECLARE @RandomNameID INT = (ABS(CHECKSUM(NEWID())) % @NameCount) + 1;
    SELECT @ProductType = Type, @CategoryID = Category, @SKU_Prefix = SKU_Pre, @ProductName = Name 
    FROM @ProductNames WHERE ID = @RandomNameID;
    
    SET @ProductName = @ProductName + ' Phiên bản ' + CAST(@Count AS NVARCHAR(3));
    
    -- Tính toán giá và tồn kho
    IF @CategoryID = 6 -- Phụ kiện
    BEGIN
        SET @BasePrice = 500000 + (ABS(CHECKSUM(NEWID())) % 2000000); 
        SET @BrandID = 4; -- Luxury Acc
        SET @Stock = 30 + (ABS(CHECKSUM(NEWID())) % 50); -- Ít tồn hơn
        SET @ShortDesc = N'Sản phẩm phụ kiện ' + @ProductType + N' cao cấp, chất liệu da thật/hợp kim.';
    END
    ELSE 
    BEGIN
        SET @BasePrice = 200000 + (ABS(CHECKSUM(NEWID())) % 800000); 
        SET @BrandID = (ABS(CHECKSUM(NEWID())) % 3) + 1; -- Elegant, Street Vibes, Sport Pro
        SET @Stock = 100 + (ABS(CHECKSUM(NEWID())) % 100);
        SET @ShortDesc = N'Thiết kế hiện đại, chất liệu co giãn tốt, phù hợp đi làm/dạo phố.';
    END

    SET @SalePrice = CASE WHEN @Count % 3 = 0 THEN @BasePrice * 0.75 ELSE NULL END; -- 1/3 sp có sale
    SET @CostPrice = @BasePrice * 0.45; 
    
    SET @Slug = REPLACE(LOWER(REPLACE(REPLACE(REPLACE(REPLACE(@ProductName, N' ', '-'), N'á', 'a'), N'đ', 'd'), N'ê', 'e')), N'--','-');
    SET @SKU_Prefix = @SKU_Prefix + RIGHT('000' + CAST(@Count AS NVARCHAR(3)), 3);
    
    -- CHÈN DỮ LIỆU VÀO BẢNG PRODUCTS
    INSERT INTO Products (
        product_name, slug, sku, category_id, brand_id, vendor_id, 
        description, short_description, price, sale_price, cost_price, 
        stock_quantity, low_stock_threshold, weight, dimensions, material, 
        is_featured, is_new_arrival, is_best_seller, is_active
    )
    VALUES (
        @ProductName, @Slug, @SKU_Prefix, @CategoryID, @BrandID, @VendorID,
        N'Mô tả chi tiết: ' + @ProductName + N'. Đây là sản phẩm nổi bật với độ bền cao và tính ứng dụng linh hoạt. Cam kết chất lượng.', 
        @ShortDesc, 
        @BasePrice, @SalePrice, @CostPrice, 
        @Stock, 10, 0.5 + (ABS(CHECKSUM(NEWID())) % 5) / 10.0, '30x20x5', N'Vải/Da Cao Cấp', 
        CASE WHEN @Count % 10 = 0 THEN 1 ELSE 0 END, -- 1/10 là nổi bật
        CASE WHEN @Count > 80 THEN 1 ELSE 0 END, -- 20 sp cuối là mới
        CASE WHEN @Count % 7 = 0 THEN 1 ELSE 0 END, -- 1/7 là bán chạy
        1
    );

    SET @ProductID = SCOPE_IDENTITY(); 

    -- CHÈN DỮ LIỆU VÀO BẢNG PRODUCT_IMAGES (2 - 4 hình ảnh cho mỗi sản phẩm)
    DECLARE @ImageCount INT = (ABS(CHECKSUM(NEWID())) % 3) + 2; -- 2 đến 4 ảnh
    DECLARE @i INT = 0;

    WHILE @i < @ImageCount
    BEGIN
        INSERT INTO Product_Images (product_id, image_url, alt_text, display_order, is_primary)
        VALUES (@ProductID, @ImageURL, N'Hình ảnh ' + CAST(@i+1 AS NVARCHAR(2)) + ' ' + @ProductName, @i, CASE WHEN @i = 0 THEN 1 ELSE 0 END);
        SET @i = @i + 1;
    END

    -- CHÈN DỮ LIỆU VÀO BẢNG PRODUCT_VARIANTS (Tối đa 2 biến thể chính: Size và Color)
    DECLARE @SizeIDs TABLE (size_id INT);
    DECLARE @ColorIDs TABLE (color_id INT);
    
    -- Phụ kiện (ID=6) thường là freesize, ít biến thể
    IF @CategoryID = 6
    BEGIN
        INSERT INTO @SizeIDs SELECT size_id FROM Sizes WHERE size_name = 'Freesize';
        INSERT INTO @ColorIDs SELECT color_id FROM Colors WHERE color_id IN (1, 2); -- Đen, Trắng
    END
    ELSE -- Thời trang, có đủ size S-XL
    BEGIN
        INSERT INTO @SizeIDs SELECT size_id FROM Sizes WHERE size_name IN ('S', 'M', 'L', 'XL');
        INSERT INTO @ColorIDs SELECT color_id FROM Colors WHERE color_id <= 5; -- 5 màu đầu
    END

    -- Tạo tất cả các tổ hợp biến thể (Size x Color)
    -- CHÚ Ý: Biến @SizeID và @ColorID đã được khai báo ở đầu khối WHILE, nên không còn lỗi
    DECLARE SizeCursor CURSOR FOR SELECT size_id FROM @SizeIDs;
    OPEN SizeCursor;
    FETCH NEXT FROM SizeCursor INTO @SizeID; -- Sử dụng @SizeID đã khai báo

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE ColorCursor CURSOR FOR SELECT color_id FROM @ColorIDs;
        OPEN ColorCursor;
        FETCH NEXT FROM ColorCursor INTO @ColorID; -- Sử dụng @ColorID đã khai báo

        WHILE @@FETCH_STATUS = 0
        BEGIN
            -- CHỈ CẦN KHAI BÁO CÁC BIẾN CHƯA ĐƯỢC KHAI BÁO
            DECLARE @PriceAdj DECIMAL(18,2) = 0;
            DECLARE @VariantStock INT = 5 + (ABS(CHECKSUM(NEWID())) % 20);
            DECLARE @VariantSKU NVARCHAR(100); -- Đã khai báo ở trên, nhưng khai báo lại để có scope rõ ràng hơn

            -- Nếu không muốn khai báo lại, có thể khai báo @VariantSKU_Inner và gán giá trị
            
            -- Điều chỉnh giá: Size XL và L tăng thêm giá
            IF (SELECT size_name FROM Sizes WHERE size_id = @SizeID) IN ('L', 'XL') AND @CategoryID != 6 SET @PriceAdj = 50000; 

            SELECT @VariantSKU = @SKU_Prefix + '-' + LEFT(s.size_name, 1) + '-' + LEFT(c.color_name, 1) 
            FROM Sizes s JOIN Colors c ON s.size_id = @SizeID AND c.color_id = @ColorID;

            -- Chèn biến thể
            INSERT INTO Product_Variants (product_id, size_id, color_id, sku, price_adjustment, stock_quantity)
            VALUES (@ProductID, @SizeID, @ColorID, @VariantSKU, @PriceAdj, @VariantStock);

            FETCH NEXT FROM ColorCursor INTO @ColorID;
        END

        CLOSE ColorCursor;
        DEALLOCATE ColorCursor;
        
        FETCH NEXT FROM SizeCursor INTO @SizeID;
    END

    CLOSE SizeCursor;
    DEALLOCATE SizeCursor;

    SET @Count = @Count + 1;
END

SET NOCOUNT OFF;

---


SELECT N'Tổng Sản phẩm' AS Type, COUNT(*) AS Total FROM Products
UNION ALL
SELECT N'Tổng Biến thể' AS Type, COUNT(*) FROM Product_Variants
UNION ALL
SELECT N'Tổng Hình ảnh' AS Type, COUNT(*) FROM Product_Images;