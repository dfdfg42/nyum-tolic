document.addEventListener("DOMContentLoaded", function () {
    const lazyImages = document.querySelectorAll("img.lazy-image");

    const observerOptions = {
        rootMargin: "500px",
        threshold: 0.1,
    };

    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove("lazy-image");
                observer.unobserve(img);
            }
        });
    }, observerOptions);

    lazyImages.forEach((img) => {
        observer.observe(img);
    });
});