# Changelog

## Product collection update
- Replaced demo catalog images and categories with the supplied category-wise product images.
- Added four local product-gallery images for every supplied category that included image files.
- Converted every supplied gallery image into a separate product card, with category-specific names and different prices.
- Connected homepage category cards to the catalog filter so each category opens its own relevant products.
- Updated the catalog so every category has exactly as many product cards as its supplied image files, with one image only on each product detail page.
- Changed all catalog and product-card images to contain mode so they appear fully without cropping or overlap.
- Added `UPLOAD_ALL_PRODUCT_IMAGES.ps1` to upload all 148 supplied product images to Cloudinary and automatically switch the website to the returned Cloudinary URLs.
- Added Cloudinary-ready secure seller image upload endpoint and example environment variables.

## Marketplace Screens Update
- Added Checkout, Payment and Order Confirmation routes using the approved warm heritage UI reference.
- Connected selected Cart items to shipping address, payment method, coupon and success confirmation interactions.
- Added local session-based checkout data, delivery selection and a safe demo order confirmation without a real payment charge.
- Applied the supplied local heritage artwork to the Product Listing, Artisan Profile, Artisan Support and Cart newsletter backgrounds.
- Made Artisan Profile tabs, Follow state, View All and support story actions interactive.
- Made Cart item selection control the displayed order total and added a clear checkout validation state.
- Added full Product Listing page matching approved Bazaar-e-Hunar heritage theme.
- Added functional search, sorting, categories, price, region, material and rating filters.
- Added Artisan Profile screen with story, stats and artisan products.
- Added persistent Cart screen with quantity controls, removal, totals, coupons and recommendations.
- Connected Navbar Products, Search and Cart controls to real routes.
- Connected Home View All and artisan cards to the new screens.
- Connected Product Detail Buy Now to Cart and Artisan Store to Artisan Profile.
- Preserved Home, Login, Register, Product Detail, Dashboard and authentication flows.
- Added catalog APIs and artisan API.
- Added Factory Method, Composite, Flyweight and Iterator patterns for catalog behavior.
- Added Strategy, Command and Memento patterns for requested cart behavior.
- Added JUnit pattern tests and live pattern demonstration endpoints.
