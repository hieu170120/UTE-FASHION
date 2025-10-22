// Analytics tracking script
const Analytics = {
    // Track product view
    trackProductView: function(productId) {
        fetch('/api/analytics/track/view', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ productId: productId })
        }).catch(err => console.error('Track view error:', err));
    },

    // Track add to cart
    trackAddToCart: function(productId, quantity) {
        fetch('/api/analytics/track/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ 
                productId: productId,
                quantity: quantity || 1
            })
        }).catch(err => console.error('Track cart error:', err));
    },

    // Track checkout start
    trackCheckout: function(shopId) {
        fetch('/api/analytics/track/checkout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ shopId: shopId })
        }).catch(err => console.error('Track checkout error:', err));
    },

    // Track checkout by product (backend derives shop)
    trackCheckoutByProduct: function(productId) {
        fetch('/api/analytics/track/checkout-by-product', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ productId: productId })
        }).catch(err => console.error('Track checkout error:', err));
    }
};

// Auto-track page views for product detail pages
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on a product detail page
    const productIdMeta = document.querySelector('meta[name="product-id"]');
    if (productIdMeta) {
        const productId = productIdMeta.getAttribute('content');
        Analytics.trackProductView(productId);
    }
});
