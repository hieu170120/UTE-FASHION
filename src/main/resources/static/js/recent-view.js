function setupProductCarousel(carouselId, prevBtnId, nextBtnId) {
    const carousel = document.getElementById(carouselId);
    const nextBtn = document.getElementById(nextBtnId);
    const prevBtn = document.getElementById(prevBtnId);
    if (!carousel || !nextBtn || !prevBtn) return;

    function updateCarouselScroll() {
        if (!carousel.querySelector('.product-card')) return;
        const card = carousel.querySelector('.product-card');
        const cardWidth = card.offsetWidth;
        const gap = parseInt(window.getComputedStyle(carousel).gap) || 16;
        const scrollAmount = cardWidth + gap;
        nextBtn.onclick = () => carousel.scrollBy({ left: scrollAmount, behavior: 'smooth' });
        prevBtn.onclick = () => carousel.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    }
    setTimeout(updateCarouselScroll, 500);
    window.addEventListener('resize', updateCarouselScroll);
}

setupProductCarousel('recentlyViewedCarousel', 'prevBtnRecently', 'nextBtnRecently');