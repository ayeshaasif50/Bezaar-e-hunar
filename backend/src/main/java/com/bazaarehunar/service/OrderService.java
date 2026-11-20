package com.bazaarehunar.service;

import com.bazaarehunar.dto.OrderRequest;
import com.bazaarehunar.model.AppUser;
import com.bazaarehunar.model.CatalogProduct;
import com.bazaarehunar.model.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Customer orders stored in MongoDB (collection "orders"). */
@Service
public class OrderService {
    public static final Set<String> STATUSES = Set.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");

    private final MongoTemplate mongoTemplate;
    private final SecureRandom random = new SecureRandom();

    public OrderService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Order create(OrderRequest request, AppUser user) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Your order has no items");
        }
        if (request.getItems().size() > 50) {
            throw new IllegalArgumentException("An order can contain at most 50 different items");
        }
        Order.Address address = request.getAddress();
        if (address == null || isBlank(address.getFullName()) || isBlank(address.getPhone())
                || isBlank(address.getAddress()) || isBlank(address.getCity()) || isBlank(address.getProvince())) {
            throw new IllegalArgumentException("Add your shipping address before placing the order");
        }
        if (isBlank(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Choose a payment method");
        }

        boolean express = "express".equalsIgnoreCase(request.getDelivery());
        Order order = new Order();
        int subtotal = 0;
        for (OrderRequest.Item requested : request.getItems()) {
            if (isBlank(requested.getProductId())) {
                throw new IllegalArgumentException("Every order item needs a product id");
            }
            if (requested.getQuantity() < 1 || requested.getQuantity() > 100) {
                throw new IllegalArgumentException("Item quantity must be between 1 and 100");
            }
            // The price comes from the catalog in MongoDB whenever the product exists.
            CatalogProduct product = mongoTemplate.findById(requested.getProductId(), CatalogProduct.class);
            int price = product != null ? product.getPrice() : requested.getPrice();
            if (price < 0) {
                throw new IllegalArgumentException("Invalid item price");
            }

            Order.Item item = new Order.Item();
            item.setProductId(requested.getProductId());
            item.setName(product != null ? product.getName() : firstNonBlank(requested.getName(), requested.getProductId()));
            item.setImage(product != null ? product.getImage() : requested.getImage());
            item.setArtisan(product != null ? product.getArtisan() : null);
            item.setArtisanId(product != null ? product.getArtisanId() : null);
            item.setPrice(price);
            item.setQuantity(requested.getQuantity());
            order.getItems().add(item);
            subtotal += price * requested.getQuantity();
        }

        String coupon = request.getCoupon() == null ? "" : request.getCoupon().trim();
        int shipping = express ? 600 : 300;
        int discount = coupon.isEmpty() ? 0 : (int) Math.round(subtotal * 0.10);

        order.setOrderNumber(newOrderNumber());
        order.setUserId(user.getId());
        order.setUserEmail(user.getEmail());
        order.setAddress(address);
        order.setDelivery(express ? "express" : "standard");
        order.setDeliveryLabel(express ? "Express Delivery, 1-3 Business Days" : "Standard Delivery, 3-7 Business Days");
        order.setEstimatedDelivery(express ? "1-3 Business Days" : "3-7 Business Days");
        order.setPaymentMethod(request.getPaymentMethod().trim());
        order.setCoupon(coupon.isEmpty() ? null : coupon);
        order.setSubtotal(subtotal);
        order.setShipping(shipping);
        order.setDiscount(discount);
        order.setTax(0);
        order.setTotal(Math.max(0, subtotal + shipping - discount));
        order.setStatus("PLACED");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(order.getCreatedAt());
        return mongoTemplate.insert(order);
    }

    public List<Order> findByUser(String email) {
        Query query = new Query(Criteria.where("userEmail").is(email)).with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Order.class);
    }

    public List<Order> findAll() {
        return mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC, "createdAt")), Order.class);
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query(Criteria.where("orderNumber").is(orderNumber)), Order.class));
    }

    public Order updateStatus(String orderNumber, String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Status must be one of " + STATUSES);
        }
        Order order = findByOrderNumber(orderNumber).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(normalized);
        order.setUpdatedAt(Instant.now());
        return mongoTemplate.save(order);
    }

    private String newOrderNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String number = "BEH" + (10000000 + random.nextInt(90000000));
            if (!mongoTemplate.exists(new Query(Criteria.where("orderNumber").is(number)), Order.class)) {
                return number;
            }
        }
        return "BEH" + System.currentTimeMillis();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String firstNonBlank(String first, String fallback) {
        return isBlank(first) ? fallback : first;
    }
}
