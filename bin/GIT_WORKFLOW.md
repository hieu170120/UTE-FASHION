# GIT WORKFLOW - UTE FASHION PROJECT

## 📋 CẤU TRÚC BRANCH

```
main (production)
  ├── develop (integration)
  ├── feature/auth-module (Người 1) ✅ DONE
  ├── feature/product-module (Người 2)
  ├── feature/order-module (Người 3)
  └── feature/payment-module (Người 4)
```

---

## 🌿 DANH SÁCH BRANCH THEO NGƯỜI

### **Người 1: Authentication & User Management** ✅
```bash
Branch: feature/auth-module
Status: COMPLETED
```

**Đã hoàn thành:**
- ✅ User Entity & Repository
- ✅ Login/Register DTO
- ✅ AuthService với BCrypt password
- ✅ AuthController
- ✅ Spring Security Config
- ✅ Login page (Thymeleaf + Bootstrap)
- ✅ Register page
- ✅ Homepage với navbar

**Files:**
- `src/main/java/com/example/demo/entity/User.java`
- `src/main/java/com/example/demo/repository/UserRepository.java`
- `src/main/java/com/example/demo/dto/LoginRequest.java`
- `src/main/java/com/example/demo/dto/RegisterRequest.java`
- `src/main/java/com/example/demo/service/AuthService.java`
- `src/main/java/com/example/demo/controller/AuthController.java`
- `src/main/java/com/example/demo/config/SecurityConfig.java`
- `src/main/resources/templates/auth/login.html`
- `src/main/resources/templates/auth/register.html`
- `src/main/resources/templates/index.html`

---

### **Người 2: Product & Category Management**
```bash
Branch: feature/product-module
Status: TODO
```

**Cần làm:**
- [ ] Product, Category, Brand Entity
- [ ] ProductRepository, CategoryRepository
- [ ] ProductService (CRUD, Search, Filter)
- [ ] ProductController
- [ ] Product list page
- [ ] Product detail page
- [ ] Admin product management

**Tạo branch:**
```bash
git checkout main
git pull origin main
git checkout -b feature/product-module
```

---

### **Người 3: Shopping Cart & Order Management**
```bash
Branch: feature/order-module
Status: TODO
```

**Cần làm:**
- [ ] Cart, CartItem Entity
- [ ] Order, OrderItem Entity
- [ ] CartService, OrderService
- [ ] CartController, OrderController
- [ ] Shopping cart page
- [ ] Checkout page
- [ ] Order history page
- [ ] Admin order management

**Tạo branch:**
```bash
git checkout main
git pull origin main
git checkout -b feature/order-module
```

---

### **Người 4: Payment, Reports & Advanced Features**
```bash
Branch: feature/payment-module
Status: TODO
```

**Cần làm:**
- [ ] Payment Entity
- [ ] PaymentService (VNPay integration)
- [ ] ReviewService, WishlistService
- [ ] Admin Dashboard
- [ ] Reports & Analytics
- [ ] WebSocket notifications

**Tạo branch:**
```bash
git checkout main
git pull origin main
git checkout -b feature/payment-module
```

---

## 🔄 WORKFLOW CHO MỖI NGƯỜI

### **1. Bắt đầu làm việc (Lần đầu)**

```bash
# Clone repository (nếu chưa có)
git clone <repository-url>
cd UTE-Fashion

# Tạo branch cho module của mình
git checkout -b feature/<tên-module>

# Ví dụ:
git checkout -b feature/product-module
```

### **2. Làm việc hàng ngày**

```bash
# Kiểm tra branch hiện tại
git branch

# Kiểm tra thay đổi
git status

# Thêm file vào staging
git add .

# Hoặc thêm file cụ thể
git add src/main/java/com/example/demo/entity/Product.java

# Commit với message rõ ràng
git commit -m "feat: add Product entity and repository"

# Push lên remote
git push origin feature/product-module
```

### **3. Commit Message Convention**

Sử dụng format: `<type>: <description>`

**Types:**
- `feat:` - Tính năng mới
- `fix:` - Sửa bug
- `docs:` - Cập nhật documentation
- `style:` - Format code (không ảnh hưởng logic)
- `refactor:` - Refactor code
- `test:` - Thêm test
- `chore:` - Cập nhật config, dependencies

**Ví dụ:**
```bash
git commit -m "feat: add product search functionality"
git commit -m "fix: resolve login validation error"
git commit -m "docs: update API documentation"
git commit -m "refactor: optimize database queries"
```

### **4. Cập nhật code từ main**

```bash
# Lưu công việc hiện tại
git add .
git commit -m "wip: save current work"

# Chuyển sang main và pull
git checkout main
git pull origin main

# Quay lại branch của mình
git checkout feature/product-module

# Merge main vào branch của mình
git merge main

# Giải quyết conflicts (nếu có)
# Sau đó:
git add .
git commit -m "merge: resolve conflicts with main"
```

### **5. Tạo Pull Request**

```bash
# Đảm bảo code đã commit hết
git status

# Push lên remote
git push origin feature/product-module

# Lên GitHub/GitLab tạo Pull Request:
# - Base: main
# - Compare: feature/product-module
# - Title: [Feature] Product & Category Management
# - Description: Mô tả chi tiết những gì đã làm
# - Assign reviewer: Người 1 (Team Leader)
```

---

## 📝 QUY TẮC COMMIT

### **DO ✅**
- Commit thường xuyên (mỗi khi hoàn thành 1 task nhỏ)
- Viết commit message rõ ràng, dễ hiểu
- Pull code từ main trước khi bắt đầu làm việc
- Test code trước khi commit
- Push code cuối ngày

### **DON'T ❌**
- Commit code lỗi
- Commit file không liên quan (node_modules, .class, target/)
- Commit trực tiếp vào main
- Force push (`git push -f`)
- Commit code chưa test

---

## 🔀 MERGE STRATEGY

### **Khi nào merge?**
1. Hoàn thành 1 feature hoàn chỉnh
2. Code đã được test kỹ
3. Không có lỗi linter
4. Pull Request đã được approve

### **Ai được merge?**
- Chỉ **Team Leader (Người 1)** được merge vào `main`
- Mỗi người tự merge trong branch của mình

### **Quy trình merge:**
```bash
# 1. Người làm: Tạo Pull Request
# 2. Team Leader: Review code
# 3. Team Leader: Comment hoặc Request changes
# 4. Người làm: Fix theo feedback
# 5. Team Leader: Approve và Merge
```

---

## 🚨 XỬ LÝ CONFLICTS

### **Khi gặp conflict:**

```bash
# 1. Pull code mới nhất từ main
git checkout main
git pull origin main

# 2. Quay lại branch của mình
git checkout feature/product-module

# 3. Merge main vào branch
git merge main

# 4. Git sẽ báo conflicts
# Mở file bị conflict, tìm các dòng:
# <<<<<<< HEAD
# Your changes
# =======
# Changes from main
# >>>>>>> main

# 5. Sửa file, giữ lại code đúng

# 6. Sau khi sửa xong
git add .
git commit -m "merge: resolve conflicts with main"
git push origin feature/product-module
```

---

## 📊 KIỂM TRA TIẾN ĐỘ

### **Xem tất cả branches:**
```bash
git branch -a
```

### **Xem lịch sử commit:**
```bash
git log --oneline --graph --all
```

### **Xem ai commit gì:**
```bash
git log --author="Tên bạn" --oneline
```

### **Xem thay đổi trong file:**
```bash
git diff src/main/java/com/example/demo/entity/Product.java
```

---

## 🛠️ LỆNH HỮU ÍCH

### **Hoàn tác thay đổi:**
```bash
# Hoàn tác file chưa staged
git restore <file>

# Hoàn tác file đã staged
git restore --staged <file>

# Hoàn tác commit cuối (giữ lại changes)
git reset --soft HEAD~1

# Hoàn tác commit cuối (xóa changes)
git reset --hard HEAD~1
```

### **Xem thông tin:**
```bash
# Branch hiện tại
git branch

# Remote repository
git remote -v

# Thay đổi chưa commit
git status

# Lịch sử commit
git log --oneline -10
```

### **Làm sạch:**
```bash
# Xóa file untracked
git clean -fd

# Xóa branch local
git branch -d feature/old-branch

# Xóa branch remote
git push origin --delete feature/old-branch
```

---

## 📞 KHI CẦN HỖ TRỢ

### **Gặp vấn đề về Git:**
1. Hỏi Team Leader (Người 1)
2. Tạo issue trên GitHub/GitLab
3. Google: "git <vấn đề>"

### **Trước khi hỏi, hãy:**
- Chụp màn hình lỗi
- Copy command đã chạy
- Mô tả bước đã làm

---

## 🎯 CHECKLIST TRƯỚC KHI PUSH

- [ ] Code chạy được không lỗi
- [ ] Đã test các tính năng mới
- [ ] Không có file không cần thiết (target/, .class)
- [ ] Commit message rõ ràng
- [ ] Đã pull code mới nhất từ main
- [ ] Không có conflicts

---

## 📚 TÀI LIỆU THAM KHẢO

- [Git Documentation](https://git-scm.com/doc)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Chúc team làm việc hiệu quả! 🚀**

**Mọi thắc mắc về Git, liên hệ Team Leader (Người 1)**

