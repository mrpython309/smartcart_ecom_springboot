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
import java.util.List;

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
                log.info("Users already exist. Updating default user and admin phone numbers...");
                updateUserPhoneNumbers();
            }

            // Ensure we always have our 60 seeded products
            long productCount = productRepository.count();
            if (productCount < 60) {
                log.info("Detected {} products. Expected 60. Seeding remaining data...", productCount);
                initializeData();
            } else {
                log.info("Database already contains {} products. Skipping data seeding.", productCount);
            }

            log.info("SmartCart Data Initialization completed successfully.");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
        }
    }

    private void initializeUsers() {
        // Create admin user
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@smartcart.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phone("9876543210")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Create test user
        User testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("User@123"))
                .phone("9876543211")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
    }

    private void updateUserPhoneNumbers() {
        userRepository.findByEmail("admin@smartcart.com").ifPresent(admin -> {
            admin.setPhone("8080811780");
            userRepository.save(admin);
            log.info("Updated admin phone number to 8080811780");
        });
        userRepository.findByEmail("john@example.com").ifPresent(user -> {
            user.setPhone("8080811780");
            userRepository.save(user);
            log.info("Updated test user phone number to 8080811780");
        });
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

        log.info("Successfully seeded 6 categories and exactly 60 premium products into MySQL.");
    }

    private void createProductsForCategory(Category category, List<String> productNames, BigDecimal minPrice,
            BigDecimal maxPrice, String defaultBrand, String seedWord) {
        List<Product> allProducts = productRepository.findAll();
        for (int i = 0; i < productNames.size(); i++) {
            String name = productNames.get(i);

            // Check if product already exists to avoid duplicates if partial seed was ran
            if (allProducts.stream().anyMatch(p -> p.getName().equals(name))) {
                continue;
            }

            BigDecimal price = minPrice.add(BigDecimal.valueOf(Math.random() * maxPrice.doubleValue())).setScale(2,
                    java.math.RoundingMode.HALF_UP);
            BigDecimal discountPrice = price.multiply(BigDecimal.valueOf(0.85)).setScale(2,
                    java.math.RoundingMode.HALF_UP); // 15% discount

            String imageUrl = null;
            if ("Apple iPhone 15 Pro".equals(name)) {
                imageUrl = "https://cdsassets.apple.com/live/7WUAS350/images/tech-specs/iphone_15_pro.png";
            } else if ("Samsung Galaxy S24 Ultra".equals(name)) {
                imageUrl = "https://vlebazaar.in/image/cache/catalog/Samsung-Galaxy-S24-Ultra-5G-AI-Smartphone-Titanium-Gray-12GB-256GB-Stora/Samsung-Galaxy-S24-Ultra-5G-AI-Smartphone-Titanium-Gray-12GB-256GB-Storage-S928B-1500x1500.jpg";
            } else if ("MacBook Air M3".equals(name)) {
                imageUrl = "https://maplestore.in/cdn/shop/files/1_M3_2024_Air_Midnight_7591747b-6f93-4d28-bd6a-09ab1c96f0a3.png?v=1779415964&width=1946";
            } else if ("Dell XPS 13".equals(name)) {
                imageUrl = "https://www.dell.com/wp-uploads/2026/05/2601g0169-dell-xps-13-dx13260-roma-in-storm-site-banners-800x620-2-1280x1280-1.png";
            } else if ("Sony WH-1000XM5 Headphones".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/61O3iMlnJIL._SX522_.jpg";
            } else if ("Apple Watch Series 9".equals(name)) {
                imageUrl = "https://rukminim2.flixcart.com/image/480/640/xif0q/smartwatch/7/r/e/45-mr993hn-a-ios-apple-yes-original-imagterzzu4fsrqg.jpeg?q=90";
            } else if ("Samsung 55-inch Crystal 4K TV".equals(name)) {
                imageUrl = "https://rukminim2.flixcart.com/image/480/480/xif0q/television/q/o/8/-original-imagyk8tsbgtfudu.jpeg?q=90";
            } else if ("PlayStation 5 Slim".equals(name)) {
                imageUrl = "https://i5.walmartimages.com/seo/PlayStation-5-Digital-Console-Slim_3d7b0255-6cf9-47e0-930b-7dd1eb46eacc.9ec69570d4fdcf76055b648b2bd52cae.png";
            } else if ("Logitech MX Master 3S Mouse".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/618IJzC-fFL._AC_UF1000,1000_QL80_.jpg";
            } else if ("Apple iPad Air M2".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71vDKKYs9nL._AC_UF1000,1000_QL80_.jpg";
            } else if ("Nike Air Zoom Pegasus 41".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71TtKQpoy7L._AC_UY1000_.jpg";
            } else if ("Adidas Ultraboost Light".equals(name)) {
                imageUrl = "https://assets.adidas.com/images/w_600,f_auto,q_auto/a09692e4425f40e28a0e3e8fc9d41c54_9366/Ultraboost_Light_Shoes_Orange_ID3277_HM1.jpg";
            } else if ("Levi's 511 Slim Fit Jeans".equals(name)) {
                imageUrl = "https://levi.in/cdn/shop/files/A70870358_01_Styleshot.jpg?v=1774266785";
            } else if ("Tommy Hilfiger Polo Shirt".equals(name)) {
                imageUrl = "https://cdn07.nnnow.com/web-images/large/styles/41E8YU2GH06/1747059019490/1.jpg";
            } else if ("Puma Essentials Hoodie".equals(name)) {
                imageUrl = "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_750,h_750/global/682571/80/mod01/fnd/IND/fmt/png/Essentials-No.-1-Logo-Men's-Comfort-Hoodie";
            } else if ("Ray-Ban Aviator Sunglasses".equals(name)) {
                imageUrl = "https://rukminim2.flixcart.com/image/480/640/xif0q/sunglass/u/e/t/-original-imahejzwcyrkzvgf.jpeg?q=90";
            } else if ("Fossil Gen 6 Smartwatch".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71Vqggkd8hL._AC_UF1000,1000_QL80_.jpg";
            } else if ("Nike Sports Backpack".equals(name)) {
                imageUrl = "https://static.nbastore.in/resized/500X500/1154/nike-varsity-elite-backpack-32l-blackmetallic-silver-blackmetallic-silver-68d3e80a628cc.jpg?format=webp";
            } else if ("Levi's Denim Jacket".equals(name)) {
                imageUrl = "https://levi.in/cdn/shop/files/248690163_01_Styleshot.jpg?v=1772529416";
            } else if ("Van Heusen Formal Suit".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71ropHpFvUL._AC_UY1100_.jpg";
            } else if ("IKEA LACK Coffee Table".equals(name)) {
                imageUrl = "https://www.ikea.com/in/en/images/products/lack-coffee-table-white__0702217_pe724349_s5.jpg?f=s";
            } else if ("Philips Smart LED Lamp".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/61WlXKWqRxL.jpg";
            } else if ("Prestige Non-Stick Cookware Set".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/618r4FsyUdL.jpg";
            } else if ("Sleepwell Memory Foam Pillow".equals(name)) {
                imageUrl = "https://mysleepwell.b-cdn.net/uploads/products/webp/1-1757932282339.webp";
            } else if ("Eureka Forbes Robot Vacuum".equals(name)) {
                imageUrl = "https://www.eurekaforbes.com/cms/assets/prod/Fully_auto_final_05_38e13776bc.jpg";
            } else if ("Milton Dinner Set".equals(name)) {
                imageUrl = "https://www.milton.in/cdn/shop/files/Opalware__Lunis__21_Pcs_Dinner_Set.jpg?v=1764140586&width=1946";
            } else if ("Bombay Dyeing Bath Towel Set".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71HxhwSt2+L.jpg";
            } else if ("Philips Aroma Diffuser".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/81HqY3rgU3L.jpg";
            } else if ("Green Soul Office Chair".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/81C1JGoS3vL._AC_UF894,1000_QL80_.jpg";
            } else if ("Home Centre Area Rug".equals(name)) {
                imageUrl = "https://media.landmarkshops.in/cdn-cgi/image/h=750,w=750,q=85,fit=cover/homecentre/1000015855140-1000015855139_01-2100.jpg";
            } else if ("Atomic Habits".equals(name)) {
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSQaquTlN_6BwmSsqJQf0QvHVPz0IpQ3IdblQ&s";
            } else if ("The Psychology of Money".equals(name)) {
                imageUrl = "https://miro.medium.com/1*8PiGSwXDQKcRTP_VFp8z1w.jpeg";
            } else if ("Rich Dad Poor Dad".equals(name)) {
                imageUrl = "https://cdn.penguin.co.in/wp-content/uploads/2023/12/9781612681139-1-scaled.jpg";
            } else if ("Deep Work".equals(name)) {
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcScfW_fgFMYRBLzDs6QhIG885ddjaXGCvpTtg&s";
            } else if ("Clean Code".equals(name)) {
                imageUrl = "https://stancalau.ro/images/articles/clean-code-book-review/CleanCode.jpg";
            } else if ("The Pragmatic Programmer".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71Nxk9VhSTL._UF1000,1000_QL80_.jpg";
            } else if ("Sapiens".equals(name)) {
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0FxCfM-_t-RbeO2sctksNPeHO2cPndNQPLg&s";
            } else if ("1984".equals(name)) {
                imageUrl = "https://www.goelprakashan.com/cdn/shop/files/1984_2048x.png?v=1743069315";
            } else if ("Dune".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/81Ua99CURsL._AC_UF1000,1000_QL80_.jpg";
            } else if ("Harry Potter Box Set".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/81uRUnI9Y3L.jpg";
            } else if ("Boldfit Yoga Mat".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71YHT7tfomL.jpg";
            } else if ("Bowflex Adjustable Dumbbells".equals(name)) {
                imageUrl = "https://www.acmefitness.com/assets/products/a71096908eeee7d5d3d5b976cfe2f79e.jpg";
            } else if ("Crossrope Jump Rope".equals(name)) {
                imageUrl = "https://www.crossrope.com/cdn/shop/files/ProdGalleryTiles_ClassicGF_06c6f378-4c0a-4f22-a98b-df1cc0338a00.jpg?v=1777291458&width=800";
            } else if ("Resistance Band Set".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/81CgJrVHnGL.jpg";
            } else if ("TriggerPoint Foam Roller".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/71qPsyAl9VL.jpg";
            } else if ("16kg Kettlebell".equals(name)) {
                imageUrl = "https://contents.mediadecathlon.com/p2720204/748b48476253090ccb6f90664dffdb34/p2720204.jpg";
            } else if ("Optimum Nutrition Whey Protein".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/41xMp4loSDL._SY300_SX300_QL70_FMwebp_.jpg";
            } else if ("Milton Steel Water Bottle".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/51Da+TBexCL._SX679_.jpg";
            } else if ("USI Boxing Gloves".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/41is8KHx+bL._SY300_SX300_QL70_FMwebp_.jpg";
            } else if ("Push-Up Board Pro".equals(name)) {
                imageUrl = "https://jalandharstyle.com/cdn/shop/files/PushupBoard-01.jpg?v=1706435254";
            } else if ("Minimalist Vitamin C Serum".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/717Kb7GUFyL.jpg";
            } else if ("Cetaphil Moisturizing Lotion".equals(name)) {
                imageUrl = "https://d2lilqwyy1nquj.cloudfront.net/variant/1771828308125-582e36ac-9e2b-4f51-a1b3-4d4801f61dcb-1771828307384.png";
            } else if ("The Ordinary Hyaluronic Acid".equals(name)) {
                imageUrl = "https://images-static.nykaa.com/media/catalog/product/2/4/244c0f5THECI00000093_a1.jpg?tr=w-500";
            } else if ("Neutrogena Ultra Sheer SPF 50".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/41XT6-ALAjL._AC_UF1000,1000_QL80_.jpg";
            } else if ("Mamaearth Charcoal Face Wash".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/61K3ZK63o9L.jpg";
            } else if ("Lakme Matte Lipstick".equals(name)) {
                imageUrl = "https://images-static.nykaa.com/media/catalog/product/d/b/db453c4LAKME00000261_M.jpg?tr=w-500";
            } else if ("Maybelline Sky High Mascara".equals(name)) {
                imageUrl = "https://distausa.com/cdn/shop/files/71MQo8pHmBL_e9ef7e31-1d67-4658-a73d-5a0237632d8b_1024x.jpg?v=1773357896";
            } else if ("Rose Quartz Facial Roller".equals(name)) {
                imageUrl = "https://suspire.in/cdn/shop/files/IMG_9095_1080x.jpg?v=1684673881";
            } else if ("Garnier Sheet Mask Pack".equals(name)) {
                imageUrl = "https://www.bbassets.com/media/uploads/p/l/40161397_16-garnier-skin-naturals-face-serum-sheet-mask-green.jpg";
            } else if ("The Derma Co Tea Tree Gel".equals(name)) {
                imageUrl = "https://m.media-amazon.com/images/I/61G5Xk1WO9L._AC_UF1000,1000_QL80_.jpg";
            }

            Product newProduct = createProduct(
                    name,
                    "Premium quality " + name + " from " + defaultBrand +
                            ". Highly rated product with premium build quality, excellent customer reviews and reliable performance.",
                    price, discountPrice,
                    100,
                    category,
                    defaultBrand,
                    4.5,
                    imageUrl);
            allProducts.add(newProduct);
        }
    }

    private Category getOrCreateCategory(String name, String description, String imageUrl) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    return categoryRepository.save(Category.builder()
                            .name(name)
                            .description(description)
                            .imageUrl(imageUrl)
                            .build());
                });
    }

    private Product createProduct(String name, String description, BigDecimal price,
            BigDecimal discountPrice, int stock, Category category,
            String brand, double rating, String imageUrl) {
        return productRepository.save(Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .discountPrice(discountPrice)
                .stock(stock)
                .category(category)
                .brand(brand)
                .rating(rating)
                .reviewCount(500)
                .imageUrl(imageUrl)
                .active(true)
                .build());
    }
}
