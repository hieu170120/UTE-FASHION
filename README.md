# **Dự án UTE Fashion - Nền tảng Thương mại Điện tử**

## **Giới thiệu**

UTE Fashion là một ứng dụng web thương mại điện tử đa chức năng được xây dựng trên nền tảng Java Spring Boot. Dự án mô phỏng một sàn giao dịch thời trang trực tuyến, nơi nhiều nhà cung cấp (Vendor) có thể đăng bán sản phẩm và người dùng có thể mua sắm, thanh toán và theo dõi đơn hàng. Hệ thống được thiết kế với kiến trúc module, phân chia vai trò người dùng rõ ràng và hỗ trợ các tính năng phức tạp như quản lý kho, xử lý đơn hàng, giao tiếp thời gian thực và phân tích dữ liệu.

## **Mục lục**

1.  [Tính năng chính](#tinh-nang-chinh)
2.  [Công nghệ sử dụng](#cong-nghe-su-dung)
3.  [Kiến trúc hệ thống](#kien-truc-he-thong)
4.  [Yêu cầu môi trường](#yeu-cau-moi-truong)
5.  [Hướng dẫn cài đặt và khởi chạy](#huong-dan-cai-dat-va-khoi-chay)
6.  [Thông tin tài khoản mặc định](#thong-tin-tai-khoan-mac-dinh)
7.  [Cấu trúc dự án](#cau-truc-du-an)
8.  [Tác giả](#tac-gia)

## **Tính năng chính**

Hệ thống hỗ trợ 5 vai trò người dùng khác nhau, mỗi vai trò có một bộ chức năng riêng biệt:

**1. Khách (Guest):**
*   Xem và duyệt sản phẩm.
*   Tìm kiếm sản phẩm theo tên.
*   Lọc sản phẩm theo danh mục, thương hiệu, khoảng giá.
*   Xem chi tiết sản phẩm.
*   Đăng ký tài khoản mới.

**2. Người dùng (Authenticated User):**
*   Bao gồm tất cả các quyền của Khách.
*   Đăng nhập, đăng xuất và quản lý thông tin cá nhân.
*   Quản lý giỏ hàng (thêm, xóa, cập nhật số lượng).
*   Thực hiện quy trình thanh toán và đặt hàng.
*   Xem lịch sử và chi tiết các đơn hàng đã đặt.
*   Viết và gửi đánh giá (review) cho sản phẩm.
*   Sử dụng tính năng chat thời gian thực với nhà cung cấp.

**3. Nhà cung cấp (Vendor):**
*   Bao gồm tất cả các quyền của Người dùng.
*   Đăng ký và quản lý thông tin cửa hàng.
*   Quản lý sản phẩm của cửa hàng (thêm, sửa, xóa).
*   Quản lý các biến thể sản phẩm (màu sắc, kích thước).
*   Xử lý và cập nhật trạng thái các đơn hàng thuộc cửa hàng mình.
*   Xem báo cáo doanh thu và phân tích bán hàng.
*   Tạo và quản lý các mã giảm giá (coupon).

**4. Người giao hàng (Shipper):**
*   Xem danh sách các đơn hàng được chỉ định để giao.
*   Cập nhật trạng thái đơn hàng (đang giao, đã giao thành công, giao thất bại).
*   Xem lịch sử các đơn hàng đã xử lý.

**5. Quản trị viên (Admin):**
*   Toàn quyền quản lý hệ thống.
*   Quản lý tài khoản của tất cả người dùng (khóa, mở khóa, phân quyền).
*   Quản lý toàn bộ sản phẩm trên sàn, có quyền phê duyệt sản phẩm do Vendor đăng.
*   Quản lý toàn bộ đơn hàng.
*   Quản lý các danh mục, thương hiệu.
*   Quản lý thông tin các nhà cung cấp và người giao hàng.
*   Xem dashboard tổng quan về hoạt động của toàn hệ thống.

## **Công nghệ sử dụng**

*   **Ngôn ngữ:** Java 17
*   **Framework:** Spring Boot 3
*   **Quản lý phụ thuộc:** Apache Maven
*   **Cơ sở dữ liệu:** SQL Server 20
*   **Truy vấn CSDL:** Spring Data JPA (Hibernate)
*   **Bảo mật:** Spring Security (Xác thực session-based, phân quyền theo vai trò)
*   **View Engine:** Thymeleaf
*   **Giao tiếp Real-time:** Spring WebSocket (cho tính năng Chat)
*   **Lưu trữ file:** Cloudinary (dùng để lưu trữ hình ảnh sản phẩm, avatar)
*   **Front-end:** HTML, CSS, JavaScript, Bootstrap

## **Kiến trúc hệ thống**

Dự án được xây dựng theo kiến trúc phân lớp (Layered Architecture), một biến thể của mô hình MVC, giúp mã nguồn được tổ chức một cách rõ ràng, dễ bảo trì và mở rộng.

*   **Controller Layer:** Tiếp nhận các HTTP request từ người dùng, gọi các phương thức xử lý logic ở tầng Service và trả về View (Thymeleaf) hoặc dữ liệu (JSON) cho client.
*   **DTO (Data Transfer Object) Layer:** Đóng vai trò là các đối tượng trung gian để truyền dữ liệu giữa các lớp, đặc biệt là giữa Controller và Service, giúp giảm sự phụ thuộc và che giấu cấu trúc của Entity.
*   **Service Layer:** Chứa toàn bộ logic nghiệp vụ của ứng dụng (ví dụ: xử lý đặt hàng, tính toán doanh thu). Lớp này giao tiếp với Repository Layer để thao tác với dữ liệu.
*   **Repository Layer:** Giao tiếp trực tiếp với cơ sở dữ liệu thông qua Spring Data JPA. Cung cấp các phương thức để truy vấn, thêm, sửa, xóa dữ liệu mà không cần viết mã SQL thủ công.
*   **Entity Layer:** Định nghĩa các thực thể ứng với các bảng trong cơ sở dữ liệu.

## **Yêu cầu môi trường**

*   **JDK:** Phiên bản 17 hoặc mới hơn.
*   **Maven:** Phiên bản 3.8 hoặc mới hơn.
*   **Cơ sở dữ liệu:** SQL Server 20.

## **Hướng dẫn cài đặt và khởi chạy**

Vui lòng thực hiện các bước sau để thiết lập và chạy dự án trên máy cục bộ.

**Bước 1: Tải mã nguồn**
```bash
git clone https://github.com/hieu170120/UTE-FASHION
cd UTE-Fashion
```

**Bước 2: Thiết lập cơ sở dữ liệu**
1. Mở trình quản lý CSDL.
2. Tạo một schema mới với tên `UTE_Fashion`.
   ```sql
   CREATE DATABASE UTE_Fashion;
   ```
3. Chạy file script `database/UTE_Fashion_Database_Schema.sql` để tạo cấu trúc bảng.
4. Chạy file script `database/UTE_Fashion_Sample_Data.sql` để thêm dữ liệu mẫu (bao gồm các tài khoản mặc định).

**Bước 3: Cấu hình kết nối**
1. Mở file `src/main/resources/application.properties`.
2. Cập nhật các thông tin sau để khớp với cấu hình CSDL của bạn

**Bước 4: Khởi chạy ứng dụng**
1. Mở Terminal hoặc Command Prompt tại thư mục gốc của dự án.
2. Chạy lệnh Maven để build dự án:
   ```bash
   mvn clean install
   ```
3. Sau khi build thành công, chạy ứng dụng bằng lệnh:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc bạn có thể mở dự án bằng một IDE (IntelliJ, Eclipse) và chạy file `UteFashionApplication.java`.

**Bước 5: Truy cập ứng dụng**
Mở trình duyệt và truy cập vào địa chỉ: `http://localhost:5055/UTE_Fashion`

## **Thông tin tài khoản mặc định**

Bạn có thể sử dụng các tài khoản sau để kiểm tra các chức năng của hệ thống (mật khẩu cho tất cả là `123456`):

*   **Admin:**
    *   Tên đăng nhập: `admin`
*   **Vendor:**
    *   Tên đăng nhập: `vendor`
*   **User:**
    *   Tên đăng nhập: `user`
*   **Shipper:**
    *   Tên đăng nhập: `shipper`

## **Cấu trúc dự án**

```
.
├── database/              -- Chứa các file script SQL
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/example/demo
│   │   │       ├── config/        -- Các lớp cấu hình (Bảo mật, DB,...)
│   │   │       ├── controller/    -- Các lớp Controller (Admin, User, API,...)
│   │   │       ├── dto/           -- Các lớp Data Transfer Object
│   │   │       ├── entity/        -- Các lớp Entity (ánh xạ bảng DB)
│   │   │       ├── enums/         -- Các kiểu dữ liệu enum
│   │   │       ├── exception/     -- Xử lý ngoại lệ toàn cục
│   │   │       ├── repository/    -- Các interface Repository (JPA)
│   │   │       ├── security/      -- Cấu hình JWT, UserDetailsService
│   │   │       ├── service/       -- Logic nghiệp vụ
│   │   │       └── UteFashionApplication.java -- Lớp khởi chạy chính
│   │   └── resources
│   │       ├── static/            -- Tài nguyên tĩnh (CSS, JS, Images)
│   │       ├── templates/         -- Các file template Thymeleaf
│   │       └── application.properties -- File cấu hình ứng dụng
│   └── test/                  -- Mã nguồn cho việc kiểm thử
├── pom.xml                    -- File cấu hình của Maven
└── README.md                  -- File thông tin dự án
```
