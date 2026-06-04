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
                log.info("Users already exist. Skipping user initialization.");
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

    private void initializeData() {
        log.info("Initializing categories and products...");

        Category electronics = getOrCreateCategory("Electronics", "Smartphones, laptops, and gadgets",
                "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400");
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
                "https://images.unsplash.com/photo-1445205170230-053b83016050?w=400");
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
                "https://images.unsplash.com/photo-1484101403633-562f891d0d67?w=400");
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
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400");
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
                "https://images.unsplash.com/photo-1461896836934-bd45ba8fcf9b?w=400");
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
                "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400");
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
            } else if ("Volumizing Mascara".equals(name)) {
                imageUrl = "https://www.lakmeindia.com/cdn/shop/files/29112_S2-8901030859073_1000x.jpg?v=1742202692";
            } else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            } else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
            }else if ("Matte Liquid Lipstick".equals(name)) {
                imageUrl = "https://www.justherbs.in/cdn/shop/products/11SoftPinkBERRY-min.jpg?v=1746536449&width=713";
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
