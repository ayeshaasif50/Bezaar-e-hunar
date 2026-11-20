# Reference UI update notes

- Replaced the old navigation with Home, Shop, Categories, Our Story, Custom Orders, and Contact.
- Added responsive category mega-menu using the existing catalog category data.
- Set the root route to the actual landing page and added dedicated Our Story, Custom Orders, and Contact routes.
- Added heritage-style page layouts using the supplied collection, artisan, footer, parchment, and gift artwork already in `frontend/public/assets/`.
- Kept the existing product catalog, product detail, cart, wishlist, checkout, payment, order confirmation, authentication, and pattern demonstration code in place.
- Custom order and contact form submissions are currently stored in browser localStorage for this frontend prototype. They are not yet persisted to the backend database because the current backend does not expose custom-order or contact-message endpoints.
- JavaScript/JSX syntax was checked. A production Vite build could not be run in this environment because frontend dependencies were not installed and npm installation timed out. Run `npm install` locally before `npm run build`.
