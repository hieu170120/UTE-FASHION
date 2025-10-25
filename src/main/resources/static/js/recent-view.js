// Function to set up the carousel arrow buttons
function setupProductCarousel(carouselId, prevBtnId, nextBtnId) {
    const carousel = document.getElementById(carouselId);
    const nextBtn = document.getElementById(nextBtnId);
    const prevBtn = document.getElementById(prevBtnId);
    if (!carousel || !nextBtn || !prevBtn) return;

    // A helper function to update scroll amounts, useful for responsive designs
    function updateCarouselScroll() {
        // Wait for images to potentially load and cards to have a width
        if (!carousel.querySelector('.product-card')) return;
        const card = carousel.querySelector('.product-card');
        const cardWidth = card.offsetWidth;
        const gap = parseInt(window.getComputedStyle(carousel).gap) || 16;
        const scrollAmount = cardWidth + gap;

        // Assign click events
        nextBtn.onclick = () => carousel.scrollBy({ left: scrollAmount, behavior: 'smooth' });
        prevBtn.onclick = () => carousel.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    }

    // Run initially and on resize. Timeout helps ensure elements are rendered.
    /*setTimeout(updateCarouselScroll, 500);*/
    window.addEventListener('resize', updateCarouselScroll);
}


// Function to initialize lazy loading for the recent view carousel
function initRecentView(config) {
    const recentlyViewedCarousel = document.getElementById('recentlyViewedCarousel');
    if (!recentlyViewedCarousel) return;

    let imagesLoaded = false; // Flag to ensure the fetch happens only once

    // This function will be called by the Intersection Observer
    const lazyLoadImages = (entries, observer) => {
        const [entry] = entries; // We are only observing one element

        if (!entry.isIntersecting || imagesLoaded) return;

        console.log('[DEBUG] Recently viewed carousel is in view. Fetching images...');
        fetchAndDisplayImages();

        imagesLoaded = true; // Mark as loaded
        observer.unobserve(entry.target); // Clean up: stop observing
    };

    // Create the observer
    const observer = new IntersectionObserver(lazyLoadImages, {
        rootMargin: '0px 0px 150px 0px', // Start loading when the carousel is 150px from the bottom of the viewport
        threshold: 0.01 // Trigger even if just 1% is visible
    });

    // Start observing the carousel
    observer.observe(recentlyViewedCarousel);

    // --- Helper Functions ---

    // 1. Fetches data and orchestrates the display
    async function fetchAndDisplayImages() {
        const productCards = recentlyViewedCarousel.querySelectorAll('.product-card[data-product-id]');
        if (productCards.length === 0) return;

        // Add spinners while loading
        addSpinners(productCards);

        const productIds = Array.from(productCards).map(card => card.dataset.productId);

        try {
            const response = await fetch(config.contextPath + 'api/products/images-for-list', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [config.csrfHeader]: config.csrfToken
                },
                body: JSON.stringify(productIds)
            });

            if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

            const imagesMap = await response.json();
            // Display the fetched images
            displayImages(productCards, imagesMap);

        } catch (error) {
            console.error('Error fetching recently viewed product images:', error);
            // Handle failure: show a placeholder
            displayPlaceholders(productCards);
        }
    }

    // 2. Adds spinners to each product card
    function addSpinners(cards) {
        cards.forEach(card => {
            const imageContainer = card.querySelector('.product-image-container');
            if (imageContainer) {
                imageContainer.innerHTML = `
                    <div class="spinner-container-recent" style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background-color: #f8f9fa;">
                        <div class="spinner-border spinner-border-sm text-secondary" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                ` + imageContainer.innerHTML; // Keep existing content like sale badge
            }
        });
    }

    // 3. Displays the actual images once fetched
    function displayImages(cards, imagesMap) {
        cards.forEach(card => {
            const productId = card.dataset.productId;
            const imageContainer = card.querySelector('.product-image-container');
            const images = imagesMap[productId];
            const fallbackUrl = config.contextPath + 'static/images/placeholder.png'; // A default placeholder
            const imageUrl = (images && images.length > 0) ? images[0].imageUrl : fallbackUrl;
            
            // Remove spinner
            const spinner = imageContainer.querySelector('.spinner-container-recent');
            if (spinner) spinner.remove();

            // Create and prepend the image to the container
            const img = document.createElement('img');
            img.src = imageUrl;
            img.alt = card.querySelector('h6').textContent;
            img.className = 'product-main-image';
            img.onerror = () => { img.src = fallbackUrl; }; // Handle broken image links
            imageContainer.prepend(img);
        });
		setupProductCarousel('recentlyViewedCarousel', 'prevBtnRecently', 'nextBtnRecently');
    }

    // 4. Displays placeholders if the API call fails
    function displayPlaceholders(cards) {
        cards.forEach(card => {
            const imageContainer = card.querySelector('.product-image-container');
            const fallbackUrl = config.contextPath + 'static/images/placeholder.png';
            
            const spinner = imageContainer.querySelector('.spinner-container-recent');
            if (spinner) spinner.remove();

            const img = document.createElement('img');
            img.src = fallbackUrl;
            img.alt = 'Image not available';
            img.className = 'product-main-image';
            imageContainer.prepend(img);
        });
		setupProductCarousel('recentlyViewedCarousel', 'prevBtnRecently', 'nextBtnRecently');
    }
}
