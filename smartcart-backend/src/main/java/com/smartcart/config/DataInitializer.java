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
@Profile({"dev", "prod"})
@SuppressWarnings("null")
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

        Category electronics = getOrCreateCategory("Electronics", "Smartphones, laptops, and gadgets", "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400");
        createProductsForCategory(electronics, Arrays.asList(
            "Smartphone X Pro", "Laptop Ultra HD", "Wireless Noise-Cancelling Earbuds", "Smart Watch Series 9",
            "4K Ultra HD Smart TV", "Bluetooth Portable Speaker", "Next-Gen Gaming Console", "Mechanical RGB Keyboard", 
            "Wireless Precision Mouse", "Pro Tablet 12-inch"
        ), new BigDecimal("100"), new BigDecimal("1500"), "TechBrand", "electronics");

        Category fashion = getOrCreateCategory("Fashion", "Clothing, shoes, and accessories", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=400");
        createProductsForCategory(fashion, Arrays.asList(
            "Classic Denim Jacket", "Premium Slim Fit Shirt", "Running Sneakers Pro", "Leather Crossbody Bag",
            "Aviator Sunglasses", "Men's Chronograph Watch", "Cotton Comfort Joggers", "Summer Floral Dress",
            "Woolen Winter Beanie", "Designer Formal Suit"
        ), new BigDecimal("20"), new BigDecimal("300"), "StyleX", "fashion");

        Category home = getOrCreateCategory("Home & Living", "Furniture, decor, and kitchen essentials", "https://images.unsplash.com/photo-1484101403633-562f891d0d67?w=400");
        createProductsForCategory(home, Arrays.asList(
            "Modern Oak Coffee Table", "Smart LED Desk Lamp", "Premium Non-Stick Cookware Set", "Orthopedic Memory Foam Pillow",
            "Robot Vacuum Cleaner", "Ceramic Dinnerware Set", "Luxury Cotton Bath Towels", "Aromatherapy Essential Oil Diffuser",
            "Ergonomic Office Chair", "Bohemian Pattern Rug"
        ), new BigDecimal("15"), new BigDecimal("600"), "HomeStyle", "furniture");

        Category books = getOrCreateCategory("Books", "Fiction, non-fiction, and educational books", "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400");
        createProductsForCategory(books, Arrays.asList(
            "Atomic Habits by James Clear", "The Psychology of Money", "1984 by George Orwell", "Sapiens by Yuval Noah Harari",
            "Dune by Frank Herbert", "Thinking, Fast and Slow", "The Great Gatsby", "Harry Potter Box Set",
            "Clean Code by Robert Martin", "The Lord of the Rings"
        ), new BigDecimal("10"), new BigDecimal("100"), "Penguin", "books");

        Category sports = getOrCreateCategory("Sports & Fitness", "Sports equipment and fitness gear", "https://images.unsplash.com/photo-1461896836934-bd45ba8fcf9b?w=400");
        createProductsForCategory(sports, Arrays.asList(
            "Premium Eco-Friendly Yoga Mat", "Adjustable Dumbbell Set (up to 25kg)", "Smart Jump Rope", "Resistance Bands Set",
            "Foam Roller for Muscle Recovery", "Kettlebell 16kg", "Whey Protein Powder", "Stainless Steel Water Flask",
            "Boxing Gloves 12oz", "Push-up Board System"
        ), new BigDecimal("12"), new BigDecimal("250"), "FitGear", "sports");

        Category beauty = getOrCreateCategory("Beauty & Health", "Skincare, makeup, and health products", "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400");
        createProductsForCategory(beauty, Arrays.asList(
            "Vitamin C Brightening Face Serum", "Organic Daily Moisturizer", "Hyaluronic Acid Toner", "SPF 50 Sunscreen",
            "Activated Charcoal Face Wash", "Matte Liquid Lipstick", "Volumizing Mascara", "Rose Quartz Facial Roller",
            "Hydrating Sheet Mask Pack", "Tea Tree Anti-Acne Gel"
        ), new BigDecimal("8"), new BigDecimal("60"), "GlowPure", "skincare");

        log.info("Successfully seeded 6 categories and exactly 60 premium products into MySQL.");
    }

    private void createProductsForCategory(Category category, List<String> productNames, BigDecimal minPrice, BigDecimal maxPrice, String defaultBrand, String seedWord) {
        List<Product> allProducts = productRepository.findAll();
        for (int i = 0; i < productNames.size(); i++) {
            String name = productNames.get(i);
            
            // Check if product already exists to avoid duplicates if partial seed was ran
            if (allProducts.stream().anyMatch(p -> p.getName().equals(name))) {
                continue;
            }

            BigDecimal price = minPrice.add(BigDecimal.valueOf(Math.random() * maxPrice.doubleValue())).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal discountPrice = price.multiply(BigDecimal.valueOf(0.85)).setScale(2, java.math.RoundingMode.HALF_UP); // 15% discount
            
            // Generate a visually distinct random placeholder using seed
            String imageUrl = "https://picsum.photos/seed/" + seedWord + i + "/500/500";
            if ("Tea Tree Anti-Acne Gel".equals(name)) {
                imageUrl = "https://aromamagic.com/cdn/shop/products/SC102044-1.jpg?v=1746431342&width=600";
            }
            
            Product newProduct = createProduct(
                name, 
                "Premium quality " + name.toLowerCase() + " with 1-year warranty and top-tier materials. Perfectly designed for everyday use.", 
                price, discountPrice, 
                (int) (Math.random() * 100) + 10, 
                category, 
                defaultBrand, 
                4.0 + (Math.random()), 
                imageUrl
            );
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
                .reviewCount((int) (Math.random() * 500) + 50)
                .imageUrl(imageUrl)
                .active(true)
                .build());
    }
}
