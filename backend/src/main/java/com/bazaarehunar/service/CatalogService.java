package com.bazaarehunar.service;

import com.bazaarehunar.model.CatalogProduct;
import com.bazaarehunar.patterns.behavioral.iterator.ProductCatalog;
import com.bazaarehunar.patterns.behavioral.iterator.ProductIterator;
import com.bazaarehunar.patterns.creational.factorymethod.CustomizableProductCreator;
import com.bazaarehunar.patterns.creational.factorymethod.ProductCreator;
import com.bazaarehunar.patterns.creational.factorymethod.StandardProductCreator;
import com.bazaarehunar.patterns.structural.composite.CategoryComposite;
import com.bazaarehunar.patterns.structural.composite.CategoryLeaf;
import com.bazaarehunar.patterns.structural.flyweight.CraftStyleFactory;
import com.bazaarehunar.patterns.structural.flyweight.CraftStyleFlyweight;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService {
    private final CraftStyleFactory styleFactory = new CraftStyleFactory();
    private final ProductCreator standardCreator = new StandardProductCreator();
    private final ProductCreator customizableCreator = new CustomizableProductCreator();
    // Built-in demo products. They are copied into MongoDB the first time (see seedDatabaseIfEmpty).
    private final List<CatalogProduct> seedCatalog = new ArrayList<>();
    private final CategoryComposite categoryTree;
    private final MongoTemplate mongoTemplate;

    public CatalogService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        categoryTree = buildCategoryTree();
        seedProducts();
    }

    /**
     * Stores the built-in products in MongoDB. Works category by category: a category's built-in products are
     * inserted only when none of them exist yet, so existing products (and anything deleted/edited later) are
     * never touched, while newly added built-in categories appear automatically on the next start.
     */
    public void seedDatabaseIfEmpty() {
        Map<String, List<CatalogProduct>> byCategory = new LinkedHashMap<>();
        for (CatalogProduct product : seedCatalog) {
            byCategory.computeIfAbsent(product.getCategory().toLowerCase(Locale.ROOT), key -> new ArrayList<>()).add(product);
        }
        for (List<CatalogProduct> group : byCategory.values()) {
            List<String> ids = group.stream().map(CatalogProduct::getId).toList();
            long existing = mongoTemplate.count(new Query(Criteria.where("_id").in(ids)), CatalogProduct.class);
            if (existing == 0) {
                mongoTemplate.insert(group, CatalogProduct.class);
            }
        }
    }

    private List<CatalogProduct> loadProducts() {
        return mongoTemplate.find(new Query().with(Sort.by("position")), CatalogProduct.class);
    }

    public List<CatalogProduct> search(String q, String category) {
        ProductCatalog catalog = new ProductCatalog(loadProducts());
        ProductIterator iterator = catalog.iterator();
        List<CatalogProduct> result = new ArrayList<>();
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        while (iterator.hasNext()) {
            CatalogProduct product = iterator.next();
            boolean queryMatch = needle.isBlank()
                    || product.getName().toLowerCase(Locale.ROOT).contains(needle)
                    || product.getCategory().toLowerCase(Locale.ROOT).contains(needle)
                    || product.getArtisan().toLowerCase(Locale.ROOT).contains(needle)
                    || product.getRegion().toLowerCase(Locale.ROOT).contains(needle)
                    || product.getMaterial().toLowerCase(Locale.ROOT).contains(needle);
            boolean categoryMatch = category == null || category.isBlank() || product.getCategory().equalsIgnoreCase(category);
            if (queryMatch && categoryMatch) result.add(product);
        }
        return result;
    }

    public Optional<CatalogProduct> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, CatalogProduct.class));
    }

    public CatalogProduct create(CatalogProduct input) {
        ProductCreator creator = input.isCustomizable() ? customizableCreator : standardCreator;
        CraftStyleFlyweight style = styleFactory.getStyle(
                value(input.getRegion(), "Pakistan"), value(input.getMaterial(), "Mixed"), "Handmade"
        );
        CatalogProduct created = creator.create(
                UUID.randomUUID().toString(), input.getName(), input.getCategory(), input.getPrice(),
                input.getRating(), input.getReviews(), input.getBadge(), input.getImage(), input.getArtisan(),
                input.getArtisanId(), input.getDescription(), style
        );
        created.setPosition(System.currentTimeMillis());
        return mongoTemplate.insert(created);
    }

    public CatalogProduct update(String id, CatalogProduct input) {
        CatalogProduct existing = findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        existing.setName(input.getName());
        existing.setCategory(input.getCategory());
        existing.setPrice(input.getPrice());
        existing.setRating(input.getRating());
        existing.setReviews(input.getReviews());
        existing.setBadge(input.getBadge());
        existing.setImage(input.getImage());
        existing.setArtisan(input.getArtisan());
        existing.setArtisanId(input.getArtisanId());
        existing.setRegion(input.getRegion());
        existing.setMaterial(input.getMaterial());
        existing.setDescription(input.getDescription());
        existing.setCustomizable(input.isCustomizable());
        CraftStyleFlyweight style = styleFactory.getStyle(value(input.getRegion(), "Pakistan"), value(input.getMaterial(), "Mixed"), "Handmade");
        existing.setStyleKey(style.key());
        return mongoTemplate.save(existing);
    }

    public void delete(String id) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), CatalogProduct.class);
    }

    public CategoryComposite getCategoryTree() { return categoryTree; }
    public int flyweightCount() { return styleFactory.cachedStyleCount(); }
    public int productCount() { return (int) mongoTemplate.count(new Query(), CatalogProduct.class); }

    private CategoryComposite buildCategoryTree() {
        return new CategoryComposite("All Categories")
                .add(new CategoryLeaf("Ajrak Shawl"))
                .add(new CategoryLeaf("Carved Wooden Decorative Bowl"))
                .add(new CategoryLeaf("Handcrafted Wooden Serving Tray"))
                .add(new CategoryLeaf("Handmade Ajrak Tote Bag"))
                .add(new CategoryLeaf("kashmiri Bags"))
                .add(new CategoryLeaf("khusa"))
                .add(new CategoryLeaf("Phulkari Dupatta"))
                .add(new CategoryLeaf("Sindhi Topi"))
                .add(new CategoryLeaf("Traditional Brass Candle Holder"))
                .add(new CategoryLeaf("Paintings"))
                .add(new CategoryLeaf("Calligraphy"))
                .add(new CategoryLeaf("Jewelry"))
                .add(new CategoryLeaf("Pottery"))
                .add(new CategoryLeaf("Home Decor"))
                .add(new CategoryLeaf("Sculptures"))
                .add(new CategoryLeaf("Hair Accessories"))
                .add(new CategoryLeaf("Jhumka"))
                .add(new CategoryLeaf("Perfumes & Attars"))
                .add(new CategoryLeaf("Glass Art"))
                .add(new CategoryLeaf("Crochet & Knitting"));
    }

    private void seedProducts() {
        addSeedVariants(standardCreator, "ajrak-shawl", "Ajrak Shawl", "Ajrak Shawl", 3900, 4.8, 32, "Bestseller", "/assets/products/ajrak-shawl/", extensionList("jpg,jpg,jpg,jpg,png"), "Zainab Bibi", "zainab-bibi", "Sindh", "Cotton Fabric", "Block Printing");
        addSeedVariants(customizableCreator, "carved-wooden-decorative-bowl", "Carved Wooden Decorative Bowl", "Carved Wooden Decorative Bowl", 3200, 4.9, 26, "New", "/assets/products/carved-wooden-decorative-bowl/", extensionList("jpg,png,png,png,png,png,png,png,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg"), "Rashid Ali", "rashid-ali", "Lahore", "Wood", "Wood Carving");
        addSeedVariants(standardCreator, "handcrafted-wooden-serving-tray", "Handcrafted Wooden Serving Tray", "Handcrafted Wooden Serving Tray", 2800, 4.7, 24, "", "/assets/products/handcrafted-wooden-serving-tray/", extensionList("png,png,jpg,jpg,jpg,jpg,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,png"), "Rashid Ali", "rashid-ali", "Lahore", "Wood", "Wood Craft");
        addSeedVariants(customizableCreator, "handmade-ajrak-tote-bag", "Handmade Ajrak Tote Bag", "Handmade Ajrak Tote Bag", 2700, 4.6, 21, "New", "/assets/products/handmade-ajrak-tote-bag/", extensionList("png,png,png,png,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg"), "Zainab Bibi", "zainab-bibi", "Sindh", "Cotton Fabric", "Textile Craft");
        addSeedVariants(standardCreator, "kashmiri-bags", "Kashmiri Bag", "kashmiri Bags", 3500, 4.8, 18, "", "/assets/products/kashmiri-bags/", extensionList("png,jpg,jpg,jpg,jpg,jpg"), "Ayesha Noor", "ayesha-noor", "Kashmir", "Fabric", "Embroidery");
        addSeedVariants(standardCreator, "phulkari-dupatta", "Phulkari Dupatta", "Phulkari Dupatta", 4200, 4.9, 34, "Bestseller", "/assets/products/phulkari-dupatta/", extensionList("png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg"), "Imran Khan", "imran-khan", "Punjab", "Embroidered Fabric", "Phulkari Embroidery");
        addSeedVariants(standardCreator, "sindhi-topi", "Sindhi Topi", "Sindhi Topi", 1800, 4.7, 20, "", "/assets/products/sindhi-topi/", extensionList("jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg"), "Ustad Karim", "ustad-karim", "Sindh", "Fabric", "Hand Stitching");
        addSeedVariants(standardCreator, "traditional-brass-candle-holder", "Traditional Brass Candle Holder", "Traditional Brass Candle Holder", 2600, 4.8, 16, "New", "/assets/products/traditional-brass-candle-holder/", extensionList("png,png,png,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,png,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg,jpg"), "Ustad Karim", "ustad-karim", "Multan", "Brass", "Metal Craft");
        seedSuppliedCategories();
    }

    /**
     * The 12 newer categories (Paintings ... Khusa) come from the same data the website uses
     * (resources/seed/supplied-category-products.json, generated from frontend suppliedCategoryProducts.js),
     * so product ids, names, prices and artisans match the frontend exactly.
     */
    private void seedSuppliedCategories() {
        try (java.io.InputStream in = getClass().getResourceAsStream("/seed/supplied-category-products.json")) {
            if (in == null) {
                System.err.println("seed/supplied-category-products.json not found; skipping extra categories");
                return;
            }
            com.fasterxml.jackson.databind.JsonNode items = new com.fasterxml.jackson.databind.ObjectMapper().readTree(in);
            for (com.fasterxml.jackson.databind.JsonNode item : items) {
                String region = item.path("region").asText("Pakistan");
                String material = item.path("material").asText("Mixed");
                CraftStyleFlyweight style = styleFactory.getStyle(region, material, "Handmade");
                CatalogProduct product = standardCreator.create(
                        item.path("id").asText(), item.path("name").asText(), item.path("category").asText(),
                        item.path("price").asInt(), item.path("rating").asDouble(), item.path("reviews").asInt(),
                        item.path("badge").asText(""), item.path("image").asText(),
                        item.path("artisan").asText(), item.path("artisanId").asText(),
                        item.path("description").asText(), style);
                product.setPosition(seedCatalog.size());
                seedCatalog.add(product);
            }
        } catch (Exception exception) {
            System.err.println("Could not load supplied category products: " + exception.getMessage());
        }
    }

    private void addSeedVariants(ProductCreator creator, String baseId, String baseName, String category, int basePrice,
                                 double rating, int reviews, String firstBadge, String imageFolder, String[] extensions,
                                 String artisan, String artisanId, String region, String material, String technique) {
        for (int index = 0; index < extensions.length; index++) {
            String badge = index == 0 ? firstBadge : (index % 5 == 0 ? "Limited" : "");
            String productName = index == 0 ? baseName : baseName + " Edition " + (index + 1);
            addSeed(creator, baseId + "-" + (index + 1), productName, category,
                    basePrice + index * 250, Math.min(5.0, rating + index * 0.05), reviews + index * 3, badge,
                    imageFolder + (index + 1) + "." + extensions[index], artisan, artisanId, region, material, technique);
        }
    }

    private String[] extensionList(String values) {
        return values.split(",");
    }

    private void addSeed(ProductCreator creator, String id, String name, String category, int price, double rating,
                         int reviews, String badge, String image, String artisan, String artisanId,
                         String region, String material, String technique) {
        CraftStyleFlyweight style = styleFactory.getStyle(region, material, technique);
        CatalogProduct product = creator.create(id, name, category, price, rating, reviews, badge, image, artisan, artisanId,
                "Authentic handmade " + name.toLowerCase(Locale.ROOT) + " created by skilled artisans.", style);
        product.setPosition(seedCatalog.size());
        seedCatalog.add(product);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
