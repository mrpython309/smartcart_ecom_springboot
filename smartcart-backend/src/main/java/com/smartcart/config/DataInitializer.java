package com.smartcart.config;

import com.smartcart.entity.Category;
import com.smartcart.entity.Product;
import com.smartcart.entity.User;
import com.smartcart.enums.Role;
import com.smartcart.repository.CategoryRepository;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({ "dev", "prod" })
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting SmartCart Data Initialization...");

        try {
            if (userRepository.count() == 0) {
                log.info("No users found. Initializing default admin and test users...");
                initializeUsers();
            } else {
                log.info("Users already exist. Skipping user initialization.");
            }

            // Ensure we always have our 60 seeded products
            long productCount = productRepository.count();
            if (productCount < 60) {
                log.info("Detected {} products. Expected 60. Seeding remaining data...", productCount);
                initializeData();
            } else {
                log.info("Database already contains {} products. Skipping initial seed.", productCount);
            }

            // Enrich all existing products with realistic ratings, review counts, stocks, and detailed descriptions
            enrichExistingProducts();

            log.info("SmartCart Data Initialization completed successfully.");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
        }
    }

    private void initializeUsers() {
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@smartcart.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phone("8080811780")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        User testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("User@123"))
                .phone("8080811780")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
    }

    private void enrichExistingProducts() {
        List<Product> products = productRepository.findAll();
        log.info("Enriching {} products with realistic metrics and descriptions...", products.size());

        for (Product p : products) {
            boolean updated = false;
            int hash = Math.abs(p.getName().hashCode());

            // 1. Varied realistic rating between 3.8 and 4.9
            double rating = 3.8 + ((hash % 12) / 10.0);
            if (rating > 4.9) rating = 4.9;
            rating = Math.round(rating * 10.0) / 10.0;
            if (p.getRating() == null || p.getRating() == 4.5 || p.getRating() == 0.0) {
                p.setRating(rating);
                updated = true;
            }

            // 2. Varied realistic review count between 45 and 3,450
            int reviewCount = 45 + (hash % 3400);
            if (p.getReviewCount() == null || p.getReviewCount() == 500 || p.getReviewCount() == 0) {
                p.setReviewCount(reviewCount);
                updated = true;
            }

            // 3. Varied realistic stock between 12 and 140
            int stock = 12 + (hash % 128);
            if (p.getStock() == null || p.getStock() == 100) {
                p.setStock(stock);
                updated = true;
            }

            // 4. Detailed custom descriptions
            if (p.getDescription() == null || p.getDescription().startsWith("Premium quality ")) {
                p.setDescription(generateDetailedDescription(p.getName(), p.getBrand()));
                updated = true;
            }

            // 5. Fix broken Cetaphil Image
            if ("Cetaphil Moisturizing Lotion".equals(p.getName())) {
                p.setImageUrl("/cetaphil.jpg");
                updated = true;
            }

            if (updated) {
                productRepository.save(p);
            }
        }
    }

    private void initializeData() {
        log.info("Initializing categories and products...");

        Category electronics = getOrCreateCategory("Electronics", "Smartphones, laptops, and gadgets",
                "https://techmerpm-devsite.azurewebsites.net/wp-content/uploads/2021/11/Consumer-Electronics-scaled.jpeg");
        createProductsForCategory(electronics, Arrays.asList(
                "Apple iPhone 15 Pro",
                "Samsung Galaxy S24 Ultra",
                "MacBook Air M3",
                "Dell XPS 13",
                "Sony WH-1000XM5 Headphones",
                "Apple Watch Series 9",
                "Samsung 55-inch Crystal 4K TV",
                "PlayStation 5 Slim",
                "Logitech MX Master 3S Mouse",
                "Apple iPad Air M2"), new BigDecimal("9995"), new BigDecimal("134900"), "Electronics", "electronics");

        Category fashion = getOrCreateCategory("Fashion", "Clothing, shoes, and accessories",
                "https://glance.com/_next/image?url=https%3A%2F%2Fglance-web.glance-cdn.com%2FCapsule_Wardrobe_Ideas_and_Aesthetic_Fashion_43a67eb13f.png&w=1920&q=75");
        createProductsForCategory(fashion, Arrays.asList(
                "Nike Air Zoom Pegasus 41",
                "Adidas Ultraboost Light",
                "Levi's 511 Slim Fit Jeans",
                "Tommy Hilfiger Polo Shirt",
                "Puma Essentials Hoodie",
                "Ray-Ban Aviator Sunglasses",
                "Fossil Gen 6 Smartwatch",
                "Nike Sports Backpack",
                "Levi's Denim Jacket",
                "Van Heusen Formal Suit"), new BigDecimal("999"), new BigDecimal("14999"), "Fashion", "fashion");

        Category home = getOrCreateCategory("Home & Living", "Furniture, decor, and kitchen essentials",
                "https://assets.architecturaldigest.in/photos/62026064b5d9eefa7e4e2ddf/master/pass/How%20to%20furnish%20your%20home%20on%20a%20budget.jpg");
        createProductsForCategory(home, Arrays.asList(
                "IKEA LACK Coffee Table",
                "Philips Smart LED Lamp",
                "Prestige Non-Stick Cookware Set",
                "Sleepwell Memory Foam Pillow",
                "Eureka Forbes Robot Vacuum",
                "Milton Dinner Set",
                "Bombay Dyeing Bath Towel Set",
                "Philips Aroma Diffuser",
                "Green Soul Office Chair",
                "Home Centre Area Rug"), new BigDecimal("499"), new BigDecimal("24999"), "Home & Living",
                "home & living");

        Category books = getOrCreateCategory("Books", "Fiction, non-fiction, and educational books",
                "https://collegeinfogeek.com/cdn-cgi/image/format=auto,slow-connection-quality=30,onerror=redirect/https://collegeinfogeek.com/wp-content/uploads/2018/11/Essential-Books.jpg");
        createProductsForCategory(books, Arrays.asList(
                "Atomic Habits",
                "The Psychology of Money",
                "Rich Dad Poor Dad",
                "Deep Work",
                "Clean Code",
                "The Pragmatic Programmer",
                "Sapiens",
                "1984",
                "Dune",
                "Harry Potter Box Set"), new BigDecimal("299"), new BigDecimal("3999"), "Books", "books");

        Category sports = getOrCreateCategory("Sports & Fitness", "Sports equipment and fitness gear",
                "https://student-cms.prd.timeshighereducation.com/sites/default/files/styles/default/public/different_sports.jpg?itok=CW5zK9vp");
        createProductsForCategory(sports, Arrays.asList(
                "Boldfit Yoga Mat",
                "Bowflex Adjustable Dumbbells",
                "Crossrope Jump Rope",
                "Resistance Band Set",
                "TriggerPoint Foam Roller",
                "16kg Kettlebell",
                "Optimum Nutrition Whey Protein",
                "Milton Steel Water Bottle",
                "USI Boxing Gloves",
                "Push-Up Board Pro"), new BigDecimal("399"), new BigDecimal("14999"), "Sports", "sports");

        Category beauty = getOrCreateCategory("Beauty & Health", "Skincare, makeup, and health products",
                "https://www.herbaldynamicsbeauty.com/cdn/shop/articles/daily_guide_to_better_skincare_2000x.jpg?v=1624918418");
        createProductsForCategory(beauty, Arrays.asList(
                "Minimalist Vitamin C Serum",
                "Cetaphil Moisturizing Lotion",
                "The Ordinary Hyaluronic Acid",
                "Neutrogena Ultra Sheer SPF 50",
                "Mamaearth Charcoal Face Wash",
                "Lakme Matte Lipstick",
                "Maybelline Sky High Mascara",
                "Rose Quartz Facial Roller",
                "Garnier Sheet Mask Pack",
                "The Derma Co Tea Tree Gel"), new BigDecimal("199"), new BigDecimal("1999"), "Skincare", "skincare");

        log.info("Successfully seeded categories and products into database.");
    }

    private void createProductsForCategory(Category category, List<String> productNames, BigDecimal minPrice,
            BigDecimal maxPrice, String defaultBrand, String seedWord) {
        List<Product> allProducts = productRepository.findAll();
        for (int i = 0; i < productNames.size(); i++) {
            String name = productNames.get(i);

            if (allProducts.stream().anyMatch(p -> p.getName().equals(name))) {
                continue;
            }

            BigDecimal price = minPrice.add(BigDecimal.valueOf(Math.random() * maxPrice.doubleValue())).setScale(2,
                    java.math.RoundingMode.HALF_UP);
            BigDecimal discountPrice = price.multiply(BigDecimal.valueOf(0.85)).setScale(2,
                    java.math.RoundingMode.HALF_UP);

            String imageUrl = getImageUrlForProduct(name);
            int hash = Math.abs(name.hashCode());

            double rating = Math.round((3.8 + ((hash % 12) / 10.0)) * 10.0) / 10.0;
            if (rating > 4.9) rating = 4.9;

            int reviewCount = 45 + (hash % 3400);
            int stock = 12 + (hash % 128);

            createProduct(
                    name,
                    generateDetailedDescription(name, defaultBrand),
                    price, discountPrice,
                    stock,
                    category,
                    defaultBrand,
                    rating,
                    reviewCount,
                    imageUrl);
        }
    }

    private Category getOrCreateCategory(String name, String description, String imageUrl) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .imageUrl(imageUrl)
                        .build()));
    }

    private Product createProduct(String name, String description, BigDecimal price,
            BigDecimal discountPrice, int stock, Category category,
            String brand, double rating, int reviewCount, String imageUrl) {
        return productRepository.save(Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .discountPrice(discountPrice)
                .stock(stock)
                .category(category)
                .brand(brand)
                .rating(rating)
                .reviewCount(reviewCount)
                .imageUrl(imageUrl)
                .active(true)
                .build());
    }

    // grabbed these from amazon/flipkart product pages
    private String getImageUrlForProduct(String name) {
        if ("Apple iPhone 15 Pro".equals(name)) return "https://cdsassets.apple.com/live/7WUAS350/images/tech-specs/iphone_15_pro.png";
        if ("Samsung Galaxy S24 Ultra".equals(name)) return "https://vlebazaar.in/image/cache/catalog/Samsung-Galaxy-S24-Ultra-5G-AI-Smartphone-Titanium-Gray-12GB-256GB-Stora/Samsung-Galaxy-S24-Ultra-5G-AI-Smartphone-Titanium-Gray-12GB-256GB-Storage-S928B-1500x1500.jpg";
        if ("MacBook Air M3".equals(name)) return "https://maplestore.in/cdn/shop/files/1_M3_2024_Air_Midnight_7591747b-6f93-4d28-bd6a-09ab1c96f0a3.png?v=1779415964&width=1946";
        if ("Dell XPS 13".equals(name)) return "https://www.dell.com/wp-uploads/2026/05/2601g0169-dell-xps-13-dx13260-roma-in-storm-site-banners-800x620-2-1280x1280-1.png";
        if ("Sony WH-1000XM5 Headphones".equals(name)) return "https://m.media-amazon.com/images/I/61O3iMlnJIL._SX522_.jpg";
        if ("Apple Watch Series 9".equals(name)) return "https://rukminim2.flixcart.com/image/480/640/xif0q/smartwatch/7/r/e/45-mr993hn-a-ios-apple-yes-original-imagterzzu4fsrqg.jpeg?q=90";
        if ("Samsung 55-inch Crystal 4K TV".equals(name)) return "https://rukminim2.flixcart.com/image/480/480/xif0q/television/q/o/8/-original-imagyk8tsbgtfudu.jpeg?q=90";
        if ("PlayStation 5 Slim".equals(name)) return "https://i5.walmartimages.com/seo/PlayStation-5-Digital-Console-Slim_3d7b0255-6cf9-47e0-930b-7dd1eb46eacc.9ec69570d4fdcf76055b648b2bd52cae.png";
        if ("Logitech MX Master 3S Mouse".equals(name)) return "https://m.media-amazon.com/images/I/618IJzC-fFL._AC_UF1000,1000_QL80_.jpg";
        if ("Apple iPad Air M2".equals(name)) return "https://m.media-amazon.com/images/I/71vDKKYs9nL._AC_UF1000,1000_QL80_.jpg";
        if ("Nike Air Zoom Pegasus 41".equals(name)) return "https://m.media-amazon.com/images/I/71TtKQpoy7L._AC_UY1000_.jpg";
        if ("Adidas Ultraboost Light".equals(name)) return "https://assets.adidas.com/images/w_600,f_auto,q_auto/a09692e4425f40e28a0e3e8fc9d41c54_9366/Ultraboost_Light_Shoes_Orange_ID3277_HM1.jpg";
        if ("Levi's 511 Slim Fit Jeans".equals(name)) return "https://levi.in/cdn/shop/files/A70870358_01_Styleshot.jpg?v=1774266785";
        if ("Tommy Hilfiger Polo Shirt".equals(name)) return "https://cdn07.nnnow.com/web-images/large/styles/41E8YU2GH06/1747059019490/1.jpg";
        if ("Puma Essentials Hoodie".equals(name)) return "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_750,h_750/global/682571/80/mod01/fnd/IND/fmt/png/Essentials-No.-1-Logo-Men's-Comfort-Hoodie";
        if ("Ray-Ban Aviator Sunglasses".equals(name)) return "https://rukminim2.flixcart.com/image/480/640/xif0q/sunglass/u/e/t/-original-imahejzwcyrkzvgf.jpeg?q=90";
        if ("Fossil Gen 6 Smartwatch".equals(name)) return "https://m.media-amazon.com/images/I/71Vqggkd8hL._AC_UF1000,1000_QL80_.jpg";
        if ("Nike Sports Backpack".equals(name)) return "https://static.nbastore.in/resized/500X500/1154/nike-varsity-elite-backpack-32l-blackmetallic-silver-blackmetallic-silver-68d3e80a628cc.jpg?format=webp";
        if ("Levi's Denim Jacket".equals(name)) return "https://levi.in/cdn/shop/files/248690163_01_Styleshot.jpg?v=1772529416";
        if ("Van Heusen Formal Suit".equals(name)) return "https://m.media-amazon.com/images/I/71ropHpFvUL._AC_UY1100_.jpg";
        if ("IKEA LACK Coffee Table".equals(name)) return "https://www.ikea.com/in/en/images/products/lack-coffee-table-white__0702217_pe724349_s5.jpg?f=s";
        if ("Philips Smart LED Lamp".equals(name)) return "https://m.media-amazon.com/images/I/61WlXKWqRxL.jpg";
        if ("Prestige Non-Stick Cookware Set".equals(name)) return "https://m.media-amazon.com/images/I/618r4FsyUdL.jpg";
        if ("Sleepwell Memory Foam Pillow".equals(name)) return "https://mysleepwell.b-cdn.net/uploads/products/webp/1-1757932282339.webp";
        if ("Eureka Forbes Robot Vacuum".equals(name)) return "https://www.eurekaforbes.com/cms/assets/prod/Fully_auto_final_05_38e13776bc.jpg";
        if ("Milton Dinner Set".equals(name)) return "https://www.milton.in/cdn/shop/files/Opalware__Lunis__21_Pcs_Dinner_Set.jpg?v=1764140586&width=1946";
        if ("Bombay Dyeing Bath Towel Set".equals(name)) return "https://m.media-amazon.com/images/I/71HxhwSt2+L.jpg";
        if ("Philips Aroma Diffuser".equals(name)) return "https://m.media-amazon.com/images/I/81HqY3rgU3L.jpg";
        if ("Green Soul Office Chair".equals(name)) return "https://m.media-amazon.com/images/I/81C1JGoS3vL._AC_UF894,1000_QL80_.jpg";
        if ("Home Centre Area Rug".equals(name)) return "https://media.landmarkshops.in/cdn-cgi/image/h=750,w=750,q=85,fit=cover/homecentre/1000015855140-1000015855139_01-2100.jpg";
        if ("Atomic Habits".equals(name)) return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQaquTlN_6BwmSsqJQf0QvHVPz0IpQ3IdblQ&s";
        if ("The Psychology of Money".equals(name)) return "https://miro.medium.com/1*8PiGSwXDQKcRTP_VFp8z1w.jpeg";
        if ("Rich Dad Poor Dad".equals(name)) return "https://cdn.penguin.co.in/wp-content/uploads/2023/12/9781612681139-1-scaled.jpg";
        if ("Deep Work".equals(name)) return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcScfW_fgFMYRBLzDs6QhIG885ddjaXGCvpTtg&s";
        if ("Clean Code".equals(name)) return "https://stancalau.ro/images/articles/clean-code-book-review/CleanCode.jpg";
        if ("The Pragmatic Programmer".equals(name)) return "https://m.media-amazon.com/images/I/71Nxk9VhSTL._UF1000,1000_QL80_.jpg";
        if ("Sapiens".equals(name)) return "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0FxCfM-_t-RbeO2sctksNPeHO2cPndNQPLg&s";
        if ("1984".equals(name)) return "https://www.goelprakashan.com/cdn/shop/files/1984_2048x.png?v=1743069315";
        if ("Dune".equals(name)) return "https://m.media-amazon.com/images/I/81Ua99CURsL._AC_UF1000,1000_QL80_.jpg";
        if ("Harry Potter Box Set".equals(name)) return "https://m.media-amazon.com/images/I/81uRUnI9Y3L.jpg";
        if ("Boldfit Yoga Mat".equals(name)) return "https://m.media-amazon.com/images/I/71YHT7tfomL.jpg";
        if ("Bowflex Adjustable Dumbbells".equals(name)) return "https://www.acmefitness.com/assets/products/a71096908eeee7d5d3d5b976cfe2f79e.jpg";
        if ("Crossrope Jump Rope".equals(name)) return "https://www.crossrope.com/cdn/shop/files/ProdGalleryTiles_ClassicGF_06c6f378-4c0a-4f22-a98b-df1cc0338a00.jpg?v=1777291458&width=800";
        if ("Resistance Band Set".equals(name)) return "https://m.media-amazon.com/images/I/81CgJrVHnGL.jpg";
        if ("TriggerPoint Foam Roller".equals(name)) return "https://m.media-amazon.com/images/I/71qPsyAl9VL.jpg";
        if ("16kg Kettlebell".equals(name)) return "https://contents.mediadecathlon.com/p2720204/748b48476253090ccb6f90664dffdb34/p2720204.jpg";
        if ("Optimum Nutrition Whey Protein".equals(name)) return "https://m.media-amazon.com/images/I/41xMp4loSDL._SY300_SX300_QL70_FMwebp_.jpg";
        if ("Milton Steel Water Bottle".equals(name)) return "https://m.media-amazon.com/images/I/51Da+TBexCL._SX679_.jpg";
        if ("USI Boxing Gloves".equals(name)) return "https://m.media-amazon.com/images/I/41is8KHx+bL._SY300_SX300_QL70_FMwebp_.jpg";
        if ("Push-Up Board Pro".equals(name)) return "https://jalandharstyle.com/cdn/shop/files/PushupBoard-01.jpg?v=1706435254";
        if ("Minimalist Vitamin C Serum".equals(name)) return "https://m.media-amazon.com/images/I/717Kb7GUFyL.jpg";
        if ("Cetaphil Moisturizing Lotion".equals(name)) return "/cetaphil.jpg";
        if ("The Ordinary Hyaluronic Acid".equals(name)) return "https://images-static.nykaa.com/media/catalog/product/2/4/244c0f5THECI00000093_a1.jpg?tr=w-500";
        if ("Neutrogena Ultra Sheer SPF 50".equals(name)) return "https://m.media-amazon.com/images/I/41XT6-ALAjL._AC_UF1000,1000_QL80_.jpg";
        if ("Mamaearth Charcoal Face Wash".equals(name)) return "https://m.media-amazon.com/images/I/61K3ZK63o9L.jpg";
        if ("Lakme Matte Lipstick".equals(name)) return "https://images-static.nykaa.com/media/catalog/product/d/b/db453c4LAKME00000261_M.jpg?tr=w-500";
        if ("Maybelline Sky High Mascara".equals(name)) return "https://distausa.com/cdn/shop/files/71MQo8pHmBL_e9ef7e31-1d67-4658-a73d-5a0237632d8b_1024x.jpg?v=1773357896";
        if ("Rose Quartz Facial Roller".equals(name)) return "https://suspire.in/cdn/shop/files/IMG_9095_1080x.jpg?v=1684673881";
        if ("Garnier Sheet Mask Pack".equals(name)) return "https://www.bbassets.com/media/uploads/p/l/40161397_16-garnier-skin-naturals-face-serum-sheet-mask-green.jpg";
        if ("The Derma Co Tea Tree Gel".equals(name)) return "https://m.media-amazon.com/images/I/61G5Xk1WO9L._AC_UF1000,1000_QL80_.jpg";
        return null;
    }

    private String generateDetailedDescription(String name, String brand) {
        Map<String, String> customDescriptions = new HashMap<>();
        customDescriptions.put("Apple iPhone 15 Pro", "Superfast A17 Pro chip, aerospace-grade titanium design, customizable Action button, and a versatile 48MP main camera system with incredible night mode rendering.");
        customDescriptions.put("Samsung Galaxy S24 Ultra", "Powered by Snapdragon 8 Gen 3 for Galaxy, features built-in S Pen, integrated Galaxy AI tools, and a revolutionary 200MP camera setup.");
        customDescriptions.put("MacBook Air M3", "Incredibly thin and fast laptop with Apple M3 chip, stunning Liquid Retina display, up to 18 hours of battery life, and silent fanless design.");
        customDescriptions.put("Dell XPS 13", "Crafted with CNC machined aluminum, InfinityEdge display, 13th Gen Intel Core performance, and seamless ultra-portable productivity.");
        customDescriptions.put("Sony WH-1000XM5 Headphones", "Industry-leading noise canceling with two processors, 8 microphones, exceptional sound quality, and crystal-clear hands-free calling.");
        customDescriptions.put("Apple Watch Series 9", "Advanced health sensors, Double Tap gesture control, brighter Always-On display, and carbon neutral case combinations.");
        customDescriptions.put("Atomic Habits", "The instant #1 New York Times bestseller by James Clear. An easy & proven way to build good habits and break bad ones.");
        customDescriptions.put("Clean Code", "A handbook of agile software craftsmanship by Robert C. Martin. Learn how to write clean, maintainable, and robust code.");
        customDescriptions.put("The Psychology of Money", "Timeless lessons on wealth, greed, and happiness by Morgan Housel. Explore how people think about money.");

        if (customDescriptions.containsKey(name)) {
            return customDescriptions.get(name);
        }

        return "Experience premium performance with " + name + " from " + brand + 
               ". Engineered for durability, high reliability, and exceptional user satisfaction with backed manufacturer warranty.";
    }
}
