# Latest UI Update

This build preserves the approved Bazaar-e-Hunar Home, Login, Register and Product Detail theme and adds three reference-matched marketplace screens from the latest UI sheet:

1. Product Listing / Shop Our Collection
2. Artisan Profile / Meet Our Artisans
3. Cart / Order Summary
4. Checkout / Shipping Address
5. Payment Method
6. Order Confirmed

All new screens reuse the maroon, cream, parchment, dark-wood footer, serif typography and handmade imagery already approved for the project.

## Supplied background images used
- The supplied artisan image is now the Meet Our Artisans hero background.
- The supplied collection image is now the Product Listing hero background.
- The supplied heritage craft image is now the Artisan Support banner background.
- The supplied gift image is now the Cart newsletter background.

These assets are included inside `frontend/public/assets/`, so the refreshed screens do not depend on those background images loading from an external website.

## Functional connections
- Navbar Products -> `/products`
- Navbar search -> Product Listing search query
- Product cards -> Product Detail
- Add to Cart -> persistent cart
- Navbar cart icon -> `/cart`
- Home artisan cards -> Artisan Profile
- Product Detail artisan store -> Artisan Profile
- Product Detail Buy Now -> adds item and opens Cart
- Cart quantity/remove/coupon/totals -> working
- Artisan follow state and profile tabs -> working
- Artisan View All -> filtered collection
- Cart item selection -> selected order summary and checkout validation
- Cart checkout -> Address -> Payment -> Order Confirmed flow
- Shipping address and delivery method -> validated before payment
- Payment method, coupon and confirmation details -> working local demo actions

No existing Home/Login/Register design was replaced.
