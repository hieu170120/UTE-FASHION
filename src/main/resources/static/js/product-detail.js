// Product Detail Page JavaScript
// Global variables (will be set from HTML with Thymeleaf)
let productId;
let csrfToken;
let csrfHeader;
let contextPath;

// Product variants data
let productVariants = [];
let uniqueSizes = [];
let uniqueColors = [];
let selectedSize = null;
let selectedColor = null;

// Review upload data
let uploadedImageUrls = [];
let uploadedVideoUrls = [];

// === IMAGE HANDLING ===
function changeMainImage(thumbnailEl) {
    const mainImage = document.getElementById('mainProductImage');
    document.querySelectorAll('.thumbnail-item img').forEach(img => img.classList.remove('active'));
    thumbnailEl.classList.add('active');

    mainImage.style.display = 'none';
    document.getElementById('main-image-placeholder').style.display = 'flex';

    const newImage = new Image();
    newImage.onload = function() {
        mainImage.src = newImage.src;
        mainImage.classList.add('loaded');
        mainImage.style.display = 'block';
        document.getElementById('main-image-placeholder').style.display = 'none';
    };
    newImage.onerror = function() {
        mainImage.src = 'https://via.placeholder.com/500?text=Error';
        mainImage.classList.add('loaded');
        mainImage.style.display = 'block';
        document.getElementById('main-image-placeholder').style.display = 'none';
        console.error("Failed to load image:", thumbnailEl.dataset.src);
    };
    newImage.src = thumbnailEl.dataset.src;
}

function loadProductImages(apiUrl) {
    const thumbnailList = document.getElementById('thumbnail-list');
    const mainImage = document.getElementById('mainProductImage');

    fetch(apiUrl).then(res => res.json()).then(images => {
        thumbnailList.innerHTML = '';
        thumbnailList.classList.remove('loading-state');
        
        if (images && images.length > 0) {
            images.forEach((img, index) => {
                const activeClass = index === 0 ? 'active' : '';
                const thumbnailHtml = `
                    <div class="thumbnail-item">
                        <img class="lazy-image"
                             src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"
                             data-src="${img.imageUrl}"
                             alt="Thumbnail ${index}"
                             class="${activeClass}"
                             onclick="changeMainImage(this)">
                    </div>`;
                thumbnailList.innerHTML += thumbnailHtml;
            });
            
            const lazyImages = thumbnailList.querySelectorAll('.lazy-image');
            lazyImages.forEach(lazyImage => {
                const image = new Image();
                image.onload = () => {
                    lazyImage.src = lazyImage.dataset.src;
                    lazyImage.classList.add('loaded');
                };
                image.src = lazyImage.dataset.src;
            });

            const firstImageSrc = images[0].imageUrl;
            mainImage.src = firstImageSrc;
            mainImage.classList.add('loaded');
            mainImage.style.display = 'block';
            document.getElementById('main-image-placeholder').style.display = 'none';
        } else {
            mainImage.src = 'https://via.placeholder.com/500';
            mainImage.classList.add('loaded');
            mainImage.style.display = 'block';
            document.getElementById('main-image-placeholder').style.display = 'none';
            thumbnailList.innerHTML = '<div class="text-muted p-2">No images</div>';
        }
    }).catch(err => {
        console.error("Error loading product images:", err);
        thumbnailList.innerHTML = '<div class="text-danger p-2">Lỗi tải ảnh.</div>';
        document.getElementById('main-image-placeholder').innerHTML = '<span class="text-danger">Lỗi tải ảnh.</span>';
    });
}

// === VARIANT HANDLING ===
function updateVariantOptions() {
    const sizeContainer = document.getElementById('size-options-container');
    const colorContainer = document.getElementById('color-options-container');
    
    sizeContainer.innerHTML = '';
    colorContainer.innerHTML = '';

    uniqueSizes = [...new Set(productVariants.map(v => v.sizeName))].filter(s => s).sort();
    uniqueColors = [...new Set(productVariants.map(v => ({ name: v.colorName, code: v.colorCode })))].filter(c => c.name);

    uniqueSizes.forEach(sizeName => {
        const isActive = sizeName === selectedSize ? 'active' : '';
        const isAvailable = productVariants.some(v => v.sizeName === sizeName && v.stock > 0);
        const isOutOfStock = !isAvailable ? 'out-of-stock' : '';
        
        sizeContainer.innerHTML += `
            <span class="size-option ${isActive} ${isOutOfStock}"
                  data-size="${sizeName}"
                  onclick="${isOutOfStock ? '' : 'selectSize(this)'}">
                ${sizeName}
            </span>`;
    });

    const renderedColors = new Set();
    uniqueColors.forEach(color => {
        if(renderedColors.has(color.name)) return;
        renderedColors.add(color.name);

        const isActive = color.name === selectedColor ? 'active' : '';
        const isAvailable = productVariants.some(v => v.colorName === color.name && v.stock > 0);
        const isOutOfStock = !isAvailable ? 'out-of-stock' : '';

        colorContainer.innerHTML += `
            <span class="color-option ${isActive} ${isOutOfStock}"
                  data-color="${color.name}"
                  onclick="${isOutOfStock ? '' : 'selectColor(this)'}">
                <span class="color-swatch" style="background-color:${color.code};"></span>
            </span>`;
    });
    
    if (uniqueSizes.length === 0 && uniqueColors.length === 0 && productVariants.length > 0) {
        sizeContainer.innerHTML = '<p class="text-muted">Không có tùy chọn kích thước.</p>';
        colorContainer.innerHTML = '<p class="text-muted">Không có tùy chọn màu sắc.</p>';
    } else if (productVariants.length === 0) {
        sizeContainer.innerHTML = '<p class="text-danger">Sản phẩm hiện đang hết hàng.</p>';
        colorContainer.innerHTML = '<p class="text-danger">Sản phẩm hiện đang hết hàng.</p>';
    }

    updateSelectionStatus();
}

function selectSize(el) {
    const newSize = el.dataset.size;
    selectedSize = selectedSize === newSize ? null : newSize;
    document.querySelectorAll('.size-option').forEach(s => s.classList.remove('active'));
    if (selectedSize) el.classList.add('active');
    updateSelectionStatus();
}

function selectColor(el) {
    const newColor = el.dataset.color;
    selectedColor = selectedColor === newColor ? null : newColor;
    document.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
    if (selectedColor) el.classList.add('active');
    updateSelectionStatus();
}

function updateSelectionStatus() {
    const stockInfo = document.getElementById('stock-info');
    const quantityInput = document.getElementById('quantity');
    const addToCartBtn = document.querySelector('.btn-add-to-cart');
    const buyNowBtn = document.querySelector('.btn-buy-now');

    const isSizeRequired = uniqueSizes.length > 0;
    const isColorRequired = uniqueColors.length > 0;
    
    if ((isSizeRequired && !selectedSize) || (isColorRequired && !selectedColor)) {
        stockInfo.innerText = "Vui lòng chọn đủ Kích thước và Màu sắc.";
        stockInfo.style.display = 'block';
        quantityInput.max = 1;
        quantityInput.value = 1;
        quantityInput.disabled = true;
        addToCartBtn.disabled = true;
        buyNowBtn.disabled = true;
        return;
    }

    const selectedVariant = productVariants.find(v => 
        (v.sizeName === selectedSize || !isSizeRequired) &&
        (v.colorName === selectedColor || !isColorRequired)
    );

    if (selectedVariant && selectedVariant.stock > 0) {
        stockInfo.style.display = 'none';
        quantityInput.max = selectedVariant.stock;
        quantityInput.value = 1;
        quantityInput.disabled = false;
        addToCartBtn.disabled = false;
        buyNowBtn.disabled = false;
    } else {
        stockInfo.innerText = "Hết hàng hoặc không tồn tại.";
        stockInfo.style.display = 'block';
        quantityInput.max = 1;
        quantityInput.value = 1;
        quantityInput.disabled = true;
        addToCartBtn.disabled = true;
        buyNowBtn.disabled = true;
    }
}

function loadProductVariants(apiUrl) {
    fetch(apiUrl)
        .then(res => res.json())
        .then(variants => {
            productVariants = variants.map(v => ({
                id: v.id,
                sku: v.sku,
                sizeName: v.size?.sizeName || null,
                colorName: v.color?.colorName || null,
                colorCode: v.color?.colorCode || null,
                stock: v.stockQuantity || 0
            }));
            updateVariantOptions();
        })
        .catch(err => {
            console.error("Error loading product variants:", err);
            document.getElementById('size-options-container').innerHTML = '<p class="text-danger">Lỗi tải kích thước.</p>';
            document.getElementById('color-options-container').innerHTML = '<p class="text-danger">Lỗi tải màu sắc.</p>';
        });
}

// === CART FUNCTIONS ===
function addToCart() {
    const isSizeRequired = uniqueSizes.length > 0;
    const isColorRequired = uniqueColors.length > 0;
    
    if ((isSizeRequired && !selectedSize) || (isColorRequired && !selectedColor)) {
        alert('Vui lòng chọn đủ Kích thước và Màu sắc!');
        return;
    }
    
    const selectedVariant = productVariants.find(v => 
        (v.sizeName === selectedSize || !isSizeRequired) &&
        (v.colorName === selectedColor || !isColorRequired)
    );
    
    if (!selectedVariant) {
        alert('Không tìm thấy sản phẩm với lựa chọn này!');
        return;
    }
    
    if (selectedVariant.stock <= 0) {
        alert('Sản phẩm đã hết hàng!');
        return;
    }
    
    const quantity = parseInt(document.getElementById('quantity').value) || 1;
    
    if (quantity > selectedVariant.stock) {
        alert(`Chỉ còn ${selectedVariant.stock} sản phẩm trong kho!`);
        return;
    }
    
    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('variantId', selectedVariant.id);
    formData.append('quantity', quantity);
    formData.append('_csrf', csrfToken);
    
    fetch(contextPath + 'cart/items', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            // Track add to cart event
            if (typeof Analytics !== 'undefined') {
                Analytics.trackAddToCart(productId, quantity);
            }
        } else {
            alert('Có lỗi xảy ra, vui lòng thử lại!');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra, vui lòng thử lại!');
    });
}

function buyNow() {
    const isSizeRequired = uniqueSizes.length > 0;
    const isColorRequired = uniqueColors.length > 0;
    
    if ((isSizeRequired && !selectedSize) || (isColorRequired && !selectedColor)) {
        alert('Vui lòng chọn đủ Kích thước và Màu sắc!');
        return;
    }
    
    const selectedVariant = productVariants.find(v => 
        (v.sizeName === selectedSize || !isSizeRequired) &&
        (v.colorName === selectedColor || !isColorRequired)
    );
    
    if (!selectedVariant) {
        alert('Không tìm thấy sản phẩm với lựa chọn này!');
        return;
    }
    
    if (selectedVariant.stock <= 0) {
        alert('Sản phẩm đã hết hàng!');
        return;
    }
    
    const quantity = parseInt(document.getElementById('quantity').value) || 1;
    
    if (quantity > selectedVariant.stock) {
        alert(`Chỉ còn ${selectedVariant.stock} sản phẩm trong kho!`);
        return;
    }
    
    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('variantId', selectedVariant.id);
    formData.append('quantity', quantity);
    formData.append('buyNow', 'true');
    formData.append('_csrf', csrfToken);
    
    fetch(contextPath + 'cart/items', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.redirect) {
            window.location.href = contextPath.replace(/\/$/, '') + data.redirect;
        } else {
            alert('Có lỗi xảy ra, vui lòng thử lại!');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra, vui lòng thử lại!');
    });
}

// === REVIEW FUNCTIONS ===
function toggleReviewForm() {
    const form = document.getElementById('review-form-container');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

function showReviewAlert(message, type) {
    const alertContainer = document.getElementById('reviewAlertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    alertContainer.appendChild(alert);
    
    setTimeout(() => alert.remove(), 5000);
}

async function uploadReviewFiles(files, type) {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    
    if (csrfToken) {
        formData.append('_csrf', csrfToken);
    }
    
    try {
        showReviewAlert(`Đang tải ${type === 'image' ? 'ảnh' : 'video'} lên...`, 'info');
        
        const token = localStorage.getItem('token');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`${contextPath}api/v1/reviews/upload/${type}s`, {
            method: 'POST',
            headers: headers,
            credentials: 'include',
            body: formData
        });
        
        if (response.status === 401 || response.status === 403) {
            showReviewAlert('Bạn cần đăng nhập để tải lên file', 'warning');
            setTimeout(() => {
                window.location.href = `${contextPath}login?redirect=${encodeURIComponent(window.location.pathname)}`;
            }, 1500);
            return;
        }
        
        if (!response.ok) {
            throw new Error('Upload failed');
        }
        
        const result = await response.json();
        
        if (result.success) {
            if (type === 'image') {
                uploadedImageUrls = uploadedImageUrls.concat(result.urls);
                displayReviewPreviews(result.urls, 'image');
            } else {
                uploadedVideoUrls = uploadedVideoUrls.concat(result.urls);
                displayReviewPreviews(result.urls, 'video');
            }
            showReviewAlert(`Tải ${type === 'image' ? 'ảnh' : 'video'} lên thành công!`, 'success');
        } else {
            showReviewAlert(result.message || 'Tải lên thất bại', 'danger');
        }
    } catch (error) {
        console.error('Upload error:', error);
        showReviewAlert(`Lỗi khi tải ${type === 'image' ? 'ảnh' : 'video'} lên`, 'danger');
    }
}

function displayReviewPreviews(urls, type) {
    const previewContainer = type === 'image' ? document.getElementById('imagePreview') : document.getElementById('videoPreview');
    
    urls.forEach(url => {
        const item = document.createElement('div');
        item.className = 'preview-item';
        
        if (type === 'image') {
            item.innerHTML = `
                <img src="${url}" alt="Preview">
                <button type="button" class="remove-btn" onclick="removeReviewFile('${url}', 'image')">
                    <i class="bi bi-x"></i>
                </button>
            `;
        } else {
            item.innerHTML = `
                <video src="${url}" muted></video>
                <button type="button" class="remove-btn" onclick="removeReviewFile('${url}', 'video')">
                    <i class="bi bi-x"></i>
                </button>
            `;
        }
        
        previewContainer.appendChild(item);
    });
}

function removeReviewFile(url, type) {
    if (type === 'image') {
        uploadedImageUrls = uploadedImageUrls.filter(u => u !== url);
        document.getElementById('imagePreview').innerHTML = '';
        displayReviewPreviews(uploadedImageUrls, 'image');
    } else {
        uploadedVideoUrls = uploadedVideoUrls.filter(u => u !== url);
        document.getElementById('videoPreview').innerHTML = '';
        displayReviewPreviews(uploadedVideoUrls, 'video');
    }
}

// === LIGHTBOX ===
function openMediaModal(url, type) {
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded');
        return;
    }
    
    const modalEl = document.getElementById('mediaModal');
    if (!modalEl) {
        console.error('Modal element not found');
        return;
    }
    
    const modal = new bootstrap.Modal(modalEl);
    const modalImage = document.getElementById('modalImage');
    const modalVideo = document.getElementById('modalVideo');
    
    if (type === 'image') {
        modalImage.src = url;
        modalImage.style.display = 'block';
        modalVideo.style.display = 'none';
        if (modalVideo) modalVideo.pause();
    } else {
        modalVideo.querySelector('source').src = url;
        modalVideo.load();
        modalVideo.style.display = 'block';
        modalImage.style.display = 'none';
    }
    
    modal.show();
}

// === INITIALIZATION ===
function initProductDetail(config) {
    // Set global variables from config
    productId = config.productId;
    csrfToken = config.csrfToken;
    csrfHeader = config.csrfHeader;
    contextPath = config.contextPath;
    
    // Load product data
    loadProductImages(config.imagesApiUrl);
    loadProductVariants(config.variantsApiUrl);
    
    // Setup review form
    const reviewComment = document.getElementById('reviewComment');
    if (reviewComment) {
        reviewComment.addEventListener('input', function() {
            const count = this.value.length;
            document.getElementById('charCount').textContent = count;
            document.getElementById('charCount').style.color = count >= 50 ? '#28a745' : '#666';
        });
    }
    
    // Image upload
    const imageUploadArea = document.getElementById('imageUploadArea');
    const imageInput = document.getElementById('imageInput');
    if (imageUploadArea && imageInput) {
        imageUploadArea.addEventListener('click', () => imageInput.click());
        imageInput.addEventListener('change', async function(e) {
            const files = Array.from(e.target.files);
            if (files.length + uploadedImageUrls.length > 5) {
                showReviewAlert('Bạn chỉ có thể tải lên tối đa 5 ảnh', 'warning');
                return;
            }
            await uploadReviewFiles(files, 'image');
            imageInput.value = '';
        });
    }
    
    // Video upload
    const videoUploadArea = document.getElementById('videoUploadArea');
    const videoInput = document.getElementById('videoInput');
    if (videoUploadArea && videoInput) {
        videoUploadArea.addEventListener('click', () => videoInput.click());
        videoInput.addEventListener('change', async function(e) {
            const files = Array.from(e.target.files);
            if (files.length + uploadedVideoUrls.length > 2) {
                showReviewAlert('Bạn chỉ có thể tải lên tối đa 2 video', 'warning');
                return;
            }
            await uploadReviewFiles(files, 'video');
            videoInput.value = '';
        });
    }
    
    // Review form submit
    const reviewForm = document.getElementById('review-form');
    if (reviewForm) {
        reviewForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = document.getElementById('submitReviewBtn');
            const originalBtnText = submitBtn.innerHTML;
            
            const rating = document.querySelector('input[name="rating"]:checked');
            if (!rating) {
                showReviewAlert('Vui lòng chọn đánh giá sao', 'warning');
                return;
            }
            
            const reviewData = {
                productId: parseInt(document.getElementById('reviewProductId').value),
                orderId: null,
                rating: parseInt(rating.value),
                title: document.getElementById('reviewTitle').value,
                comment: document.getElementById('reviewComment').value,
                imageUrls: uploadedImageUrls,
                videoUrls: uploadedVideoUrls
            };
            
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang gửi...';
            
            try {
                const token = localStorage.getItem('token');
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }
                
                if (token) {
                    headers['Authorization'] = `Bearer ${token}`;
                }
                
                const response = await fetch(`${contextPath}api/v1/reviews`, {
                    method: 'POST',
                    headers: headers,
                    credentials: 'include',
                    body: JSON.stringify(reviewData)
                });
                
                if (response.ok) {
                    showReviewAlert('Đánh giá của bạn đã được gửi thành công!', 'success');
                    reviewForm.reset();
                    uploadedImageUrls = [];
                    uploadedVideoUrls = [];
                    document.getElementById('imagePreview').innerHTML = '';
                    document.getElementById('videoPreview').innerHTML = '';
                    document.getElementById('charCount').textContent = '0';
                    
                    setTimeout(() => {
                        toggleReviewForm();
                        location.reload();
                    }, 2000);
                } else if (response.status === 401 || response.status === 403) {
                    showReviewAlert('Bạn cần đăng nhập để đánh giá sản phẩm', 'warning');
                    setTimeout(() => {
                        window.location.href = `${contextPath}login?redirect=${encodeURIComponent(window.location.pathname)}`;
                    }, 1500);
                } else {
                    const error = await response.text();
                    showReviewAlert(error || 'Có lỗi xảy ra khi gửi đánh giá', 'danger');
                }
            } catch (error) {
                console.error('Submit error:', error);
                showReviewAlert('Có lỗi xảy ra: ' + error.message, 'danger');
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
            }
        });
    }
    
    // Modal cleanup
    const mediaModal = document.getElementById('mediaModal');
    if (mediaModal) {
        mediaModal.addEventListener('hidden.bs.modal', function() {
            const modalVideo = document.getElementById('modalVideo');
            if (modalVideo) {
                modalVideo.pause();
                const source = modalVideo.querySelector('source');
                if (source) source.src = '';
            }
            const modalImage = document.getElementById('modalImage');
            if (modalImage) modalImage.src = '';
        });
    }
    
    // Check initial wishlist status
    checkWishlistStatus();
}

// === WISHLIST FUNCTIONS ===
function checkWishlistStatus() {
    fetch(`${contextPath}api/wishlist/check/${productId}`)
        .then(res => res.json())
        .then(data => {
            const wishlistBtn = document.getElementById('wishlistBtn');
            const wishlistIcon = document.getElementById('wishlistIcon');
            
            if (wishlistBtn && wishlistIcon && data.inWishlist) {
                wishlistIcon.classList.remove('bi-heart');
                wishlistIcon.classList.add('bi-heart-fill');
                wishlistBtn.classList.add('active');
            }
        })
        .catch(error => console.error('Error checking wishlist:', error));
}

function toggleWishlist() {
    const wishlistBtn = document.getElementById('wishlistBtn');
    const wishlistIcon = document.getElementById('wishlistIcon');
    const isActive = wishlistBtn.classList.contains('active');
    
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    
    if (isActive) {
        // Remove from wishlist
        fetch(`${contextPath}api/wishlist/remove?productId=${productId}`, {
            method: 'DELETE',
            headers: headers,
            credentials: 'include'
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                wishlistIcon.classList.remove('bi-heart-fill');
                wishlistIcon.classList.add('bi-heart');
                wishlistBtn.classList.remove('active');
            }
        })
        .catch(error => console.error('Error removing from wishlist:', error));
    } else {
        // Add to wishlist
        fetch(`${contextPath}api/wishlist/add?productId=${productId}`, {
            method: 'POST',
            headers: headers,
            credentials: 'include'
        })
        .then(res => {
            if (res.status === 401) {
                alert('Vui lòng đăng nhập để thêm vào danh sách yêu thích');
                throw new Error('Unauthorized');
            }
            return res.json();
        })
        .then(data => {
            if (data.success) {
                wishlistIcon.classList.remove('bi-heart');
                wishlistIcon.classList.add('bi-heart-fill');
                wishlistBtn.classList.add('active');
            }
        })
        .catch(error => {
            if (error.message !== 'Unauthorized') {
                console.error('Error adding to wishlist:', error);
            }
        });
    }
}
