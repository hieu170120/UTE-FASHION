# BẢNG PHÂN CÔNG NHIỆM VỤ CHI TIẾT - DỰ ÁN UTE FASHION
## Website Shop Thời Trang Online - 4 Thành Viên FULL-STACK

---

## 📋 TỔNG QUAN DỰ ÁN

**Công nghệ sử dụng:**
- Backend: Spring Boot + JPA + JWT + Spring Security
- Frontend: Thymeleaf + Bootstrap 5 + JavaScript
- Database: SQL Server
- Real-time: WebSocket
- Version Control: Git

**Thời gian dự kiến:** 8-10 tuần

**Nguyên tắc phân công:**
- ✅ Mỗi người phụ trách 1 module HOÀN CHỈNH (cả Backend + Frontend)
- ✅ Mỗi người làm việc full-stack trên module của mình
- ✅ Tất cả đều hiểu và code được cả BE + FE

---

## 👥 PHÂN CÔNG THEO MODULE FULL-STACK

### **THÀNH VIÊN 1: MODULE AUTHENTICATION & USER MANAGEMENT** 🔐

#### **Vai trò:** Team Leader + Full-stack Developer

#### **Backend Tasks:**

**Tuần 1-2: Project Setup & Database**
- [x] Khởi tạo Spring Boot project (Maven) ✅ DONE
- [x] Cấu hình SQL Server connection ✅ DONE
- [x] Setup JPA/Hibernate ✅ DONE
- [x] Thiết kế Database Schema (ERD) cho toàn dự án ✅ DONE
- [x] Setup Git repository ✅ DONE
- [x] Entity: User, Role, Permission ✅ DONE
- [x] Repository: UserRepository, RoleRepository ✅ DONE

**Tuần 3-4: Authentication Backend**
- [x] JWT Token Service ✅ DONE
  - [x] Generate token ✅ DONE
  - [x] Validate token ✅ DONE
  - [x] Refresh token ✅ DONE
- [x] Spring Security configuration ✅ DONE
- [x] API: POST /api/auth/register ✅ DONE
- [x] API: POST /api/auth/login ✅ DONE
- [x] API: POST /api/auth/logout ✅ DONE
- [x] Password encryption (BCrypt) ✅ DONE
- [x] UserService: CRUD operations ✅ DONE

**Tuần 5-6: User Management Backend**
- [ ] API: GET /api/users (admin only)
- [ ] API: GET /api/users/{id}
- [ ] API: PUT /api/users/{id}
- [ ] API: DELETE /api/users/{id}
- [ ] API: PUT /api/users/{id}/change-password
- [ ] API: POST /api/users/forgot-password
- [ ] Email Service (password reset)
- [ ] User profile update service
- [ ] OTP Service cho đăng ký và quên mật khẩu

**Tuần 7-8: Advanced User Features**
- [ ] API: GET /api/users/profile
- [ ] API: PUT /api/users/profile
- [ ] API: POST /api/users/upload-avatar
- [ ] API: GET /api/users/addresses
- [ ] API: POST /api/users/addresses
- [ ] API: PUT /api/users/addresses/{id}
- [ ] API: DELETE /api/users/addresses/{id}
- [ ] Address management service

#### **Frontend Tasks:**

**Tuần 3-4: Auth UI**
- [x] Setup Thymeleaf + Bootstrap ✅ DONE
- [x] Setup Decorator Sitemesh (Header/Footer layout) ✅ DONE
- [x] Trang đăng ký (`/register`) ✅ DONE
- [x] Trang đăng nhập (`/login`) ✅ DONE
- [x] JWT token handling (localStorage/cookie) ✅ DONE

**Tuần 5-6: User Profile UI**
- [ ] Trang profile user (`/profile`)
  - View profile
  - Edit profile form
  - Change password form
  - Upload avatar
- [ ] Admin: User management page (`/admin/users`)
  - List users table
  - Search/filter users
  - Block/unblock user
  - View user details modal
- [ ] Trang quên mật khẩu (`/forgot-password`)
- [ ] Trang xác thực OTP (`/verify-otp`)

**Tuần 7-8: Address Management UI**
- [ ] Trang quản lý địa chỉ (`/addresses`)
  - List addresses
  - Add new address form
  - Edit address form
  - Set default address
  - Delete address
- [ ] Address selector trong checkout
- [ ] Responsive design testing

**Deliverables:**
- ✅ Đăng ký/Đăng nhập hoàn chỉnh với JWT
- ✅ User profile management
- ✅ Admin user management
- ✅ Email service với OTP
- ✅ Address management system

---

### **THÀNH VIÊN 2: MODULE PRODUCT & CATEGORY MANAGEMENT** 🛍️

#### **Vai trò:** Full-stack Developer

#### **Backend Tasks:**

**Tuần 1-2: Database & Entities**
- [ ] Làm quen với project structure do Thành viên 1 setup
- [ ] Entity: Product, ProductImage ✅ PARTIAL
- [ ] Entity: Category (tree structure) ✅ PARTIAL
- [ ] Entity: Brand, Size, Color ✅ PARTIAL
- [ ] Repository: ProductRepository, CategoryRepository ✅ PARTIAL
- [ ] Repository: BrandRepository ✅ PARTIAL

**Tuần 3-4: Product Backend**
- [ ] API: GET /api/products (list, pagination)
- [ ] API: GET /api/products/{id}
- [ ] API: POST /api/products (admin)
- [ ] API: PUT /api/products/{id} (admin)
- [ ] API: DELETE /api/products/{id} (admin)
- [ ] API: GET /api/products/search?q=...
- [ ] API: GET /api/products/filter?category=&brand=&price=
- [ ] ProductService: Business logic
- [ ] Image upload service (local/cloud)
- [ ] API: GET /api/products/featured (sản phẩm nổi bật)
- [ ] API: GET /api/products/new-arrivals (sản phẩm mới)
- [ ] API: GET /api/products/best-sellers (sản phẩm bán chạy)

**Tuần 5-6: Category & Brand Backend**
- [ ] API: GET /api/categories
- [ ] API: POST /api/categories (admin)
- [ ] API: PUT /api/categories/{id} (admin)
- [ ] API: DELETE /api/categories/{id} (admin)
- [ ] API: GET /api/brands
- [ ] API: POST /api/brands (admin)
- [ ] API: PUT /api/brands/{id} (admin)
- [ ] API: DELETE /api/brands/{id} (admin)
- [ ] CategoryService: Tree structure handling
- [ ] API: GET /api/categories/{id}/products

**Tuần 7-8: Advanced Product Features**
- [ ] API: GET /api/products/{id}/variants
- [ ] API: POST /api/products/{id}/variants (admin)
- [ ] API: PUT /api/products/{id}/variants/{variantId} (admin)
- [ ] API: DELETE /api/products/{id}/variants/{variantId} (admin)
- [ ] Product variant management
- [ ] Inventory management
- [ ] Product recommendation service

#### **Frontend Tasks:**

**Tuần 3-4: Product Display UI**
- [ ] Homepage (`/`)
  - Featured products carousel
  - New arrivals section
  - Best sellers section
  - Category navigation
- [ ] Trang danh sách sản phẩm (`/products`)
  - Grid layout responsive
  - Pagination
  - Sort options (newest, price, popularity)
  - Filter sidebar (category, brand, price range)
- [ ] Trang chi tiết sản phẩm (`/products/{id}`)
  - Image gallery/slider
  - Product info
  - Size/color selector
  - Add to cart button
  - Product variants
  - Related products

**Tuần 5-6: Search & Admin UI**
- [ ] Search bar component (global)
- [ ] Advanced search page (`/search`)
- [ ] Filter sidebar component
- [ ] Admin: Product management (`/admin/products`)
  - List products table
  - Add product form (multiple images)
  - Edit product form
  - Delete confirmation
  - Bulk actions
- [ ] Admin: Category management (`/admin/categories`)
  - Category tree view
  - CRUD forms
  - Drag & drop reorder
- [ ] Admin: Brand management (`/admin/brands`)
  - Brand list table
  - Add/edit brand forms
  - Brand logo upload

**Tuần 7-8: Advanced Features**
- [ ] Product comparison page
- [ ] Product wishlist integration
- [ ] Recently viewed products
- [ ] Product reviews integration
- [ ] SEO optimization
- [ ] Image lazy loading
- [ ] Responsive testing

**Deliverables:**
- ✅ Product catalog với search/filter
- ✅ Product detail page
- ✅ Admin product/category/brand management
- ✅ Image upload system
- ✅ Product variants management

---

### **THÀNH VIÊN 3: MODULE SHOPPING CART & ORDER MANAGEMENT** 🛒

#### **Vai trò:** Full-stack Developer

#### **Backend Tasks:**

**Tuần 1-2: Database & Entities**
- [ ] Làm quen với project structure
- [ ] Entity: Cart, CartItem ✅ PARTIAL
- [ ] Entity: Order, OrderItem ✅ PARTIAL
- [ ] Entity: OrderStatus (enum) ✅ PARTIAL
- [ ] Entity: OrderStatusHistory ✅ PARTIAL
- [ ] Repository: CartRepository, OrderRepository ✅ PARTIAL

**Tuần 3-4: Shopping Cart Backend**
- [ ] API: GET /api/cart
- [ ] API: POST /api/cart/items (add to cart)
- [ ] API: PUT /api/cart/items/{id} (update quantity)
- [ ] API: DELETE /api/cart/items/{id}
- [ ] API: DELETE /api/cart (clear cart)
- [ ] CartService: Calculate total, discount
- [ ] Session-based cart (guest) + DB cart (logged user)
- [ ] API: GET /api/cart/count (số lượng items)

**Tuần 5-6: Order Management Backend**
- [ ] API: POST /api/orders (create order from cart)
- [ ] API: GET /api/orders (user order history)
- [ ] API: GET /api/orders/{id}
- [ ] API: PUT /api/orders/{id}/cancel
- [ ] API: GET /api/admin/orders (all orders)
- [ ] API: PUT /api/admin/orders/{id}/status
- [ ] OrderService: Order processing logic
- [ ] Email notification (order confirmation)
- [ ] Inventory update after order
- [ ] API: GET /api/orders/{id}/tracking

**Tuần 7-8: Advanced Order Features**
- [ ] API: POST /api/orders/{id}/return (trả hàng)
- [ ] API: GET /api/orders/{id}/return-history
- [ ] API: PUT /api/admin/orders/{id}/approve-return
- [ ] Order return service
- [ ] Refund processing
- [ ] Order analytics service
- [ ] WebSocket: Real-time order notifications

#### **Frontend Tasks:**

**Tuần 3-4: Shopping Cart UI**
- [ ] Add to cart button (AJAX)
- [ ] Cart icon + item count (header)
- [ ] Cart dropdown preview
- [ ] Trang giỏ hàng (`/cart`)
  - Cart items table
  - Update quantity
  - Remove item
  - Total calculation
  - Proceed to checkout button
- [ ] Empty cart state
- [ ] Cart persistence (localStorage)

**Tuần 5-6: Checkout & Order UI**
- [ ] Trang checkout (`/checkout`)
  - Multi-step form (Shipping → Payment → Review)
  - Shipping address form
  - Payment method selection
  - Order summary
  - Place order button
- [ ] Order success page (`/order-success`)
- [ ] Trang lịch sử đơn hàng (`/orders`)
  - List orders
  - Order status badge
  - View details button
  - Filter by status
- [ ] Trang chi tiết đơn hàng (`/orders/{id}`)
  - Order info
  - Order items
  - Tracking status
  - Cancel button
  - Return request button

**Tuần 7-8: Admin & Advanced Features**
- [ ] Admin: Order management (`/admin/orders`)
  - List all orders
  - Filter by status
  - Update order status
  - View order details
  - Print invoice
  - Bulk actions
- [ ] Admin: Order analytics dashboard
- [ ] WebSocket: Real-time order notifications
- [ ] Order tracking page
- [ ] Return request management
- [ ] Responsive testing

**Deliverables:**
- ✅ Shopping cart đầy đủ chức năng
- ✅ Checkout process hoàn chỉnh
- ✅ Order management cho user và admin
- ✅ Real-time notifications
- ✅ Order tracking system
- ✅ Return/refund system

---

### **THÀNH VIÊN 4: MODULE PAYMENT, REPORTS & ADVANCED FEATURES** 💳

#### **Vai trò:** Full-stack Developer

#### **Backend Tasks:**

**Tuần 1-2: Database & Setup**
- [ ] Làm quen với project structure
- [ ] Entity: Payment, PaymentMethod
- [ ] Entity: Discount, Coupon
- [ ] Entity: Review, Rating
- [ ] Entity: Wishlist
- [ ] Repository tương ứng

**Tuần 3-4: Payment Backend**
- [ ] API: POST /api/payment/create
- [ ] API: GET /api/payment/callback (VNPay/MoMo)
- [ ] API: GET /api/payment/{id}
- [ ] PaymentService: Integration với payment gateway
  - VNPay integration
  - MoMo integration (optional)
- [ ] Payment verification
- [ ] Refund handling
- [ ] API: GET /api/payment-methods

**Tuần 5-6: Advanced Features Backend**
- [ ] API: GET /api/reviews/product/{id}
- [ ] API: POST /api/reviews (add review)
- [ ] API: PUT /api/reviews/{id}
- [ ] API: DELETE /api/reviews/{id}
- [ ] API: GET /api/wishlist
- [ ] API: POST /api/wishlist/items
- [ ] API: DELETE /api/wishlist/items/{id}
- [ ] API: POST /api/coupons/apply
- [ ] API: GET /api/admin/coupons
- [ ] API: POST /api/admin/coupons (create coupon)
- [ ] ReviewService, WishlistService
- [ ] CouponService (validate, apply discount)

**Tuần 7-8: Reports & Analytics Backend**
- [ ] API: GET /api/admin/reports/sales
- [ ] API: GET /api/admin/reports/revenue
- [ ] API: GET /api/admin/reports/products
- [ ] API: GET /api/admin/reports/customers
- [ ] API: GET /api/admin/reports/orders
- [ ] ReportService: Statistics calculation
- [ ] Export to Excel/PDF
- [ ] API: GET /api/admin/dashboard/stats

#### **Frontend Tasks:**

**Tuần 3-4: Payment UI**
- [ ] Payment method selection UI
- [ ] Payment processing page (`/payment`)
  - Loading spinner
  - Redirect to payment gateway
- [ ] Payment callback page (`/payment/callback`)
  - Success message
  - Failed message
- [ ] Payment history page (`/payments`)
- [ ] Payment method management

**Tuần 5-6: Advanced Features UI**
- [ ] Product review section
  - Star rating
  - Review form
  - List reviews
  - Review images/videos
- [ ] Wishlist page (`/wishlist`)
  - List wishlist items
  - Add to cart from wishlist
  - Remove from wishlist
- [ ] Wishlist icon (header)
- [ ] Coupon code input (checkout page)
- [ ] Admin: Coupon management (`/admin/coupons`)
  - Create coupon form
  - List coupons
  - Coupon usage stats

**Tuần 7-8: Dashboard & Reports**
- [ ] Admin Dashboard (`/admin`)
  - Statistics cards (revenue, orders, users)
  - Sales chart (Chart.js)
  - Recent orders table
  - Low stock alerts
  - Popular products
  - Customer analytics
- [ ] Admin: Reports page (`/admin/reports`)
  - Date range selector
  - Sales report table/chart
  - Revenue report
  - Product performance report
  - Customer report
  - Export buttons (Excel, PDF)
- [ ] WebSocket: Live dashboard updates
- [ ] Responsive testing

**Deliverables:**
- ✅ Payment integration hoàn chỉnh (VNPay/MoMo)
- ✅ Review & rating system
- ✅ Wishlist functionality
- ✅ Coupon/discount system
- ✅ Admin dashboard với charts
- ✅ Reports & analytics
- ✅ Export functionality

---

## 📅 TIMELINE CHI TIẾT

### **TUẦN 1-2: FOUNDATION & SETUP**
**Tất cả cùng làm:**
- [x] Setup môi trường development ✅ DONE
- [x] Clone repository ✅ DONE
- [x] Setup SQL Server database ✅ DONE
- [x] Tạo database schema chung ✅ DONE
- [x] Setup branch cho từng người ✅ DONE

**Riêng từng người:**
- **Thành viên 1:** Project initialization, shared config ✅ DONE
- **Thành viên 2-4:** Tạo entities và repositories cho module của mình

**Daily sync:** Share progress, resolve conflicts

---

### **TUẦN 3-4: CORE FEATURES BACKEND + FRONTEND**
**Mỗi người:**
- Hoàn thành **Backend APIs** chính của module
- Bắt đầu làm **Frontend UI** cho module
- Test APIs bằng Postman

**Integration:**
- Thành viên 1 phải hoàn thành Auth trước (others depend on it)
- Thành viên 2 cần product APIs để thành viên 3,4 dùng

**Weekly meeting:** Demo progress

---

### **TUẦN 5-6: ADVANCED FEATURES & INTEGRATION**
**Mỗi người:**
- Hoàn thành tất cả APIs còn lại
- Hoàn thành UI pages
- Tích hợp frontend với backend của mình
- Cross-module integration

**Integration points:**
- Thành viên 3 cần APIs từ Thành viên 2 (products) và Thành viên 1 (auth)
- Thành viên 4 cần APIs từ Thành viên 3 (orders)

**Code review:** Review lẫn nhau

---

### **TUẦN 7-8: TESTING, POLISH & DEPLOYMENT**
**Tất cả cùng làm:**
- [ ] Integration testing toàn hệ thống
- [ ] Fix bugs
- [ ] Performance optimization
- [ ] Security audit
- [ ] Responsive testing
- [ ] Cross-browser testing
- [ ] Write documentation
- [ ] Prepare deployment

**Final tasks:**
- User acceptance testing
- Bug fixing
- Code cleanup
- Deployment to server

---

## 🔄 WORKFLOW & COLLABORATION

### **Git Branching Strategy**
```
main (production)
  ├── develop (integration branch)
      ├── feature/auth-module (Thành viên 1) ✅ DONE
      ├── feature/product-module (Thành viên 2)
      ├── feature/order-module (Thành viên 3)
      └── feature/payment-module (Thành viên 4)
```

### **Daily Workflow**
**Morning (9:00 AM - 9:15 AM):**
- Daily standup (15 phút)
- Yesterday / Today / Blockers

**Working Hours:**
- Code trong module của mình
- Commit thường xuyên
- Push cuối ngày

**Evening:**
- Update task board
- Review code của người khác (if requested)

### **Weekly Workflow**
**Thứ 2:** Planning cho tuần mới
**Thứ 6:** 
- Weekly demo (mỗi người demo module của mình)
- Retrospective
- Merge vào develop branch

---

## 🤝 DEPENDENCIES & COMMUNICATION

### **Module Dependencies:**
```
Module Auth (Thành viên 1) ✅ DONE
  └── Required by: ALL modules (authentication)

Module Product (Thành viên 2)
  └── Required by: Order (Thành viên 3), Payment (Thành viên 4)

Module Order (Thành viên 3)
  └── Required by: Payment (Thành viên 4)

Module Payment (Thành viên 4)
  └── Independent (but needs Order APIs)
```

### **Communication Rules:**
1. **Blocker?** → Thông báo ngay trên group
2. **API changed?** → Thông báo cho người phụ thuộc
3. **Need help?** → Tag người có thể giúp
4. **Found bug?** → Create GitHub Issue, assign người phụ trách

### **Shared Responsibilities:**
- **Code review:** Review code của nhau
- **Testing:** Test cross-module integration
- **Documentation:** Document APIs của mình
- **Meetings:** Attend all standups and weekly meetings

---

## 📊 TIẾN ĐỘ & TRACKING

### **Task Board (Trello/Jira):**
**Columns:**
- 📋 Backlog
- 🏗️ In Progress
- 👀 In Review
- ✅ Done

**Mỗi người tự quản lý tasks của module mình**

### **Progress Tracking:**
| Tuần | Thành viên 1 | Thành viên 2 | Thành viên 3 | Thành viên 4 |
|------|-------------|-------------|-------------|-------------|
| 1-2  | 80% ✅      | 20%         | 20%         | 20%         |
| 3-4  | 90% ✅      | 50%         | 50%         | 50%         |
| 5-6  | 100% ✅     | 80%         | 80%         | 80%         |
| 7-8  | 100% ✅     | 100%        | 100%        | 100%        |

---

## 🛠️ CÔNG CỤ & SETUP

### **Development Environment:**
```
IDE: IntelliJ IDEA / Spring Tool Suite
JDK: Java 17+
Database: SQL Server 2019+
Maven: 3.8+
Node.js: (nếu cần build tools)
```

### **Project Management:**
- Git: GitHub/GitLab
- Task Board: Trello
- Communication: Zalo/Discord
- API Docs: Swagger UI
- Database: SQL Server Management Studio

### **Shared Resources:**
- ERD: Draw.io
- API Documentation: Swagger/Postman Collection
- Coding Standards: Google Java Style Guide
- Git Commit Convention: Conventional Commits

---

## 📚 KIẾN THỨC YÊU CẦU (FULL-STACK)

### **Backend Skills:**
- ✅ Spring Boot (Controllers, Services, Repositories)
- ✅ Spring Data JPA (Entities, Relationships)
- ✅ Spring Security + JWT
- ✅ RESTful API design
- ✅ SQL Server
- ✅ Exception handling
- ✅ Validation

### **Frontend Skills:**
- ✅ Thymeleaf template engine
- ✅ Bootstrap 5 (Grid, Components)
- ✅ JavaScript/jQuery (AJAX calls)
- ✅ HTML5/CSS3
- ✅ Responsive design
- ✅ Form validation

### **DevOps Skills:**
- ✅ Git (branch, merge, pull request)
- ✅ Maven (dependencies, build)
- ✅ Deployment (Tomcat/Docker)

### **Learning Resources:**
- Spring Boot Documentation
- Baeldung tutorials
- Thymeleaf docs
- Bootstrap docs
- Stack Overflow

---

## ✅ DEFINITION OF DONE

**Một module được coi là DONE khi:**
- ✅ Backend APIs hoạt động đúng (test bằng Postman)
- ✅ Frontend UI hoàn chỉnh và responsive
- ✅ Integration giữa FE-BE hoạt động
- ✅ Unit tests pass
- ✅ Code được review và approve
- ✅ APIs được document (Swagger)
- ✅ Merge vào develop branch thành công
- ✅ Demo được trước team

---

## 🎯 SUCCESS CRITERIA

### **Technical Requirements:**
- ✅ User có thể đăng ký, đăng nhập (JWT)
- ✅ Browse products với filter, search
- ✅ Add to cart, checkout, payment
- ✅ Order tracking và history
- ✅ Admin quản lý products, orders, users
- ✅ Real-time notifications (WebSocket)
- ✅ Email notifications
- ✅ Reports và analytics
- ✅ Responsive trên mobile/tablet/desktop
- ✅ Security (SQL injection, XSS prevention)

### **Code Quality:**
- ✅ Clean code, readable
- ✅ Follow naming conventions
- ✅ Proper error handling
- ✅ No critical bugs
- ✅ Test coverage > 60%

### **Teamwork:**
- ✅ All members contribute equally
- ✅ Good communication
- ✅ Meet deadlines
- ✅ Help each other

---

## 📝 DELIVERABLES CUỐI CÙNG

**1. Source Code:**
- Complete Spring Boot project
- Git repository với history rõ ràng

**2. Database:**
- SQL Server database với sample data
- Database schema diagram (ERD)

**3. Documentation:**
- README.md (setup guide)
- API Documentation (Swagger)
- User Manual
- Admin Manual

**4. Presentation:**
- PowerPoint slides
- Demo video (5-10 phút)
- Live demo

**5. Reports:**
- Project report
- Individual contribution report
- Lessons learned

---

## 🔧 CÁC CHỨC NĂNG CÒN THIẾU CẦN BỔ SUNG

### **Chức năng chung:**
- [ ] Tìm kiếm và lọc sản phẩm (đã có cơ bản)
- [ ] Đăng ký tài khoản có gửi mã OTP kích hoạt qua Email
- [ ] Quên mật khẩu có gửi mã OTP kích hoạt qua Email
- [ ] Mật khẩu được mã hóa (đã có BCrypt)

### **Guest:**
- [ ] Giao diện Trang chủ (hiển thị sản phẩm bán trên 10 sản phẩm của các shop)

### **User:**
- [ ] Trang sản phẩm theo danh mục
- [ ] 20 sản phẩm mới, bán chạy, đánh giá, yêu thích nhất được phân trang
- [ ] Trang profile user (quản lý địa chỉ nhận hàng khác nhau)
- [ ] Trang chi tiết sản phẩm
- [ ] Giỏ hàng được lưu trên database
- [ ] Thanh toán (COD, VNPAY, MOMO)
- [ ] Quản lý lịch sử mua hàng theo trạng thái
- [ ] Thích sản phẩm
- [ ] Sản phẩm đã xem
- [ ] Đánh giá sản phẩm đã mua
- [ ] Bình luận sản phẩm đã mua (text tối thiểu 50 ký tự, hình ảnh/video)
- [ ] Chọn mã giảm giá

### **Vendor (Seller):**
- [ ] Đăng ký shop
- [ ] Quản lý trang chủ shop
- [ ] Quản lý sản phẩm của mình
- [ ] Quản lý đơn hàng của shop theo trạng thái
- [ ] Tạo chương trình khuyến mãi
- [ ] Quản lý doanh thu của shop

### **Admin:**
- [ ] Tìm kiếm và quản lý user
- [ ] Quản lý sản phẩm của từng shop
- [ ] Quản lý doanh mục
- [ ] Quản lý chiết khấu app cho các shop
- [ ] Quản lý chương trình khuyến mãi
- [ ] Quản lý nhà vận chuyển

### **Shipper:**
- [ ] Quản lý đơn hàng được phân công đi giao
- [ ] Thống kê đơn hàng được phân công giao

---

## 📋 CHECKLIST TRIỂN KHAI

### **Thành viên 1 - Authentication & User Management:**
- [x] Setup project và database ✅
- [x] JWT authentication ✅
- [x] Login/Register pages ✅
- [ ] User profile management
- [ ] Address management
- [ ] OTP email service
- [ ] Admin user management
- [ ] Password reset

### **Thành viên 2 - Product & Category Management:**
- [ ] Product entities và repositories
- [ ] Product CRUD APIs
- [ ] Category management
- [ ] Brand management
- [ ] Product search và filter
- [ ] Product detail page
- [ ] Admin product management
- [ ] Image upload system

### **Thành viên 3 - Shopping Cart & Order Management:**
- [ ] Cart entities và repositories
- [ ] Shopping cart functionality
- [ ] Order management
- [ ] Checkout process
- [ ] Order tracking
- [ ] Order history
- [ ] Admin order management
- [ ] Return/refund system

### **Thành viên 4 - Payment, Reports & Advanced Features:**
- [ ] Payment integration (VNPay/MoMo)
- [ ] Review và rating system
- [ ] Wishlist functionality
- [ ] Coupon system
- [ ] Admin dashboard
- [ ] Reports và analytics
- [ ] Export functionality
- [ ] WebSocket notifications

---

## 🎯 MỤC TIÊU CUỐI CÙNG

**Sau khi hoàn thành dự án, website sẽ có:**
1. ✅ Hệ thống authentication hoàn chỉnh với JWT
2. ✅ Catalog sản phẩm với search/filter
3. ✅ Shopping cart và checkout process
4. ✅ Order management và tracking
5. ✅ Payment integration
6. ✅ Review và rating system
7. ✅ Admin dashboard với reports
8. ✅ Responsive design
9. ✅ Real-time notifications
10. ✅ Email services

**Tất cả các chức năng sẽ được tích hợp hoàn chỉnh và hoạt động mượt mà trên cả desktop và mobile.**
