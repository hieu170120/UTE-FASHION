// Analytics tracking script
const Analytics = {
    // Track product view
    trackProductView: function(productId) {
        const url = (window.CONTEXT_PATH || '/UTE_Fashion/') + 'api/analytics/track/view';
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ productId: productId })
        }).catch(err => console.error('Track view error:', err));
    },

    // Track add to cart
    trackAddToCart: function(productId, quantity) {
        const url = (window.CONTEXT_PATH || '/UTE_Fashion/') + 'api/analytics/track/cart';
        fetch(url, {
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
        const url = (window.CONTEXT_PATH || '/UTE_Fashion/') + 'api/analytics/track/checkout';
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ shopId: shopId })
        }).catch(err => console.error('Track checkout error:', err));
    },

    // Track checkout by product (backend derives shop)
    trackCheckoutByProduct: function(productId) {
        const url = (window.CONTEXT_PATH || '/UTE_Fashion/') + 'api/analytics/track/checkout-by-product';
        fetch(url, {
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
