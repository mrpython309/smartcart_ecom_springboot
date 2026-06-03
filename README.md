<<<<<<< HEAD
# 🛒 SmartCart — Enterprise E-Commerce Platform

[![CI/CD](https://github.com/YOUR_USERNAME/smartcart/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/smartcart/actions/workflows/ci.yml)

A production-grade, high-performance e-commerce platform built using a modern decoupled architecture. The backend is powered by **Spring Boot**, **Spring Security + JWT**, and **MySQL**, utilizing **Redis** for sub-millisecond read caching, **Docker** for containerized orchestration, and comprehensive **JUnit 5 / Mockito** suites for robust test coverage.
=======
# SmartCart — Full-Stack E-Commerce Platform

A production-ready e-commerce application built with **Spring Boot** (backend) and **React + Vite + Tailwind CSS** (frontend).
>>>>>>> 8a60032c0a6a32520fb8a8ca5ab444043e8ac884

---

## 🚀 Tech Stack

<<<<<<< HEAD
### ☕ Backend (Enterprise Java)
*   **Java 25 + Spring Boot 3.5**
*   **Spring Security & JWT Authentication** (Stateless, role-based RBAC with rate limiting)
*   **Spring Data JPA & Hibernate** (Optimistic locking, custom query indexing)
*   **MySQL 8** (Primary transactional database)
*   **Redis Caching** (Highly optimized for product catalog, search queries, and categories)
*   **JUnit 5 & Mockito** (20+ unit test scenarios covering critical path business logic)
*   **Spring Boot Actuator** (Production observability, metrics, health checks)
*   **Swagger / OpenAPI 3.0** (Self-documenting interactive API Playground, grouped by modules — dev only)

### ⚛️ Frontend (Client Application)
*   **React 18 + Vite 5**
*   **Tailwind CSS 3.4**
*   **React Router 6**
*   **Axios + Recharts** (Interactive administrative analytics)
*   **React Hot Toast** (Micro-animations and state notifications)
*   **Error Boundary** (Graceful crash recovery)

### 🛡️ Security & Hardening
*   **Rate Limiting** (10 req/min on auth endpoints per IP)
*   **Security Headers** (HSTS, X-Frame-Options, X-Content-Type-Options, CSP, Referrer-Policy)
*   **JWT Secret Validation** (App fails to start in prod if using default secret)
*   **Profile Guards** (Debug endpoints, seed data, migrations disabled in production)

### 🏗️ DevOps
*   **Docker & Docker Compose** (Multi-stage builds, non-root containers, health checks)
*   **GitHub Actions CI/CD** (Build, test, Docker image verification)
*   **Nginx** (Reverse proxy with gzip, security headers, static asset caching)
*   **Render Blueprint** (One-click cloud deployment)
=======
### Backend
- Java 17 + Spring Boot 3.2
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL Database
- Swagger/OpenAPI Documentation
- Maven

### Frontend
- React 18 + Vite 5
- Tailwind CSS 3.4
- React Router 6
- Axios + Recharts
- React Hot Toast
>>>>>>> 8a60032c0a6a32520fb8a8ca5ab444043e8ac884

---

## 📦 Features

<<<<<<< HEAD
- 🔐 **Stateless JWT Security:** RBAC with custom `UserDetailsService`, secure BCrypt password encoding.
- ⚡ **Sub-Millisecond Redis Caching:** Optimized listing queries and detail retrievals with granular TTL policies.
- 📈 **Real-Time Analytics Dashboard:** Administrative visualization of daily revenue and order trends.
- 🛍 **E-Commerce Critical Paths:** Granular product filtering, real-time cart persistence, checkout address routing, and payment lifecycle callbacks.
- 💳 **Razorpay Payment Gateway:** Payment integration with HMAC-SHA256 signature verification, automatic refunds on cancellation.
- 📁 **Production Observability:** Built-in actuator endpoints for live application health monitoring.
- 🛡️ **Production Security:** Rate limiting, security headers, JWT validation, profile-guarded dev tools.

---

## 🏗 System & Caching Architecture
=======
- ✅ JWT-based Authentication (User + Admin roles)
- ✅ Product browsing with search, filter, sort, pagination
- ✅ Shopping cart with real-time updates
- ✅ Checkout with address management
- ✅ Order placement and tracking
- ✅ Mock payment processing
- ✅ User dashboard (profile, addresses, orders)
- ✅ Admin panel with **Real-time Analytics Dashboard**
- ✅ Admin product/order/user/category management
- ✅ Image upload support
- ✅ Swagger API documentation
- ✅ **Production Observability:** Spring Boot Actuator integration
- ✅ **Automated Testing:** JUnit 5, Mockito, and Spring MockMvc
- ✅ Responsive design (mobile + desktop)

---

## 🏗 System Architecture
>>>>>>> 8a60032c0a6a32520fb8a8ca5ab444043e8ac884

```mermaid
graph TD
    Client[React Frontend / Vite] -->|HTTPS / JSON| API[Spring Boot API]
    API -->|Auth| Security[Spring Security / JWT]
<<<<<<< HEAD
    API -->|Rate Limit| RateLimit[Rate Limit Filter]
    API -->|1. Check Cache| Redis[(Redis Cache)]
    API -->|2. Database Query| DB[(MySQL Database)]
    API -->|Metrics & Health| Actuator[Spring Boot Actuator]
    API -->|Interactive Docs| Swagger[OpenAPI / Swagger UI]
```

### ⚡ Caching Strategy (Redis)
To minimize database load and ensure maximum throughput, SmartCart implements Spring's `@Cacheable` abstraction backed by **Redis**:
1.  **Product Lists (`products`):** Cached for 10 minutes (TTL). Automatically invalidated (`@CacheEvict`) upon administrative creation, modification, or soft-deletion of products.
2.  **Product Details (`product-detail`):** Cached for 15 minutes by unique ID. Invalidated immediately when that specific product is updated or deleted.
3.  **Categories (`categories`):** Cached for 30 minutes. Invalidated on category updates.

---

## ⚙️ Development & Quickstart

### 📋 Prerequisites
*   **Java 25+**
*   **Node.js 22+**
*   **Docker & Docker Compose** (highly recommended)

### 🐳 1. Run Everything via Docker Compose (Recommended)
You can start the entire application—including the MySQL database, Redis instance, backend, and frontend—with a single command:
```bash
docker compose up --build
```
*   **Frontend:** `http://localhost:3000`
*   **Backend REST API:** `http://localhost:8080`
*   **Swagger UI (API Docs):** `http://localhost:8080/swagger-ui.html`

### 🔑 Default Credentials (Dev Profile Only)

> ⚠️ These credentials are **only created in the `dev` profile**. In production, no default users are seeded.

| Role  | Email               | Password   |
|-------|---------------------|------------|
| Admin | `admin@smartcart.com` | `Admin@123`  |
| User  | `john@example.com`    | `User@123`   |

---

## 🧪 Testing Suite (JUnit 5 + Mockito)

SmartCart implements a professional test suite featuring Mockito mocking, boundary verification, and custom exception asserting:
*   **AuthServiceTest:** Registration validation, duplicate email assertions, password encoding checks, login state verification.
*   **ProductServiceTest:** Caching check logic, exception assertions for invalid IDs, paged response transformations.
*   **CartServiceTest:** Inactive product blocks, stock boundary assertions, cart generation on demand.
*   **OrderServiceTest:** Stock deduction, address validation, cancel status propagation and stock recovery.
*   **ProductControllerIntegrationTest:** End-to-end API integration tests.

Run the test suite using Maven:
```bash
cd smartcart-backend
mvn clean test
=======
    API -->|DataAccess| JPA[Spring Data JPA]
    JPA -->|SQL| DB[(MySQL Database)]
    API -->|Monitoring| Actuator[Spring Boot Actuator]
    API -->|Docs| Swagger[OpenAPI / Swagger UI]
```

---

## 💎 Technical Rigor

This project demonstrates several high-level engineering standards:
- **Clean Architecture:** Strict separation of DTOs, Controllers, Services, and Repositories.
- **Security First:** Stateless JWT authentication with secure password hashing and Role-Based Access Control (RBAC).
- **Automated Testing:** Unit tests for business logic and Integration tests for API endpoints.
- **Observability:** Built-in health checks and system metrics via Actuator endpoints.
- **Optimized DevOps:** Dockerized environment with multi-stage builds and automated database readiness synchronization.

---

## ⚙️ Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0+ / MariaDB
- Maven 3.8+

---

## 🛠️ Installation & Setup

### 1. Database Setup
Create a database named `smartcart_db` on port `3307` (default development port configured in application.properties) or update `smartcart-backend/src/main/resources/application.properties` to match your local setup:
```sql
CREATE DATABASE IF NOT EXISTS smartcart_db;
```

### 2. Backend
Navigate to the backend directory and run:
```bash
cd smartcart-backend

# Package and run the Spring Boot application
./mvnw spring-boot:run
```
Backend will start at **http://localhost:8080**  
Swagger API Docs: **http://localhost:8080/swagger-ui.html**

### 3. Frontend
Navigate to the frontend directory, install dependencies, and run:
```bash
cd smartcart-frontend

# Install dependencies
npm install

# Start Vite development server
npm run dev
```
Frontend will start at **http://localhost:5173**

---

## 🚀 Easy Start Scripts (Windows)

For your convenience, I have provided two scripts to start the application with one click:

1. **`start-app.bat`**: Runs the entire application (DB + Backend + Frontend) using **Docker Compose**. Recommended for a consistent environment.
2. **`run-locally.bat`**: Runs the Backend (Maven) and Frontend (NPM) in parallel windows. Use this if you want to run directly on your host machine (ensure MySQL is running on port 3306).

---

## 🔑 Default Credentials

| Role  | Email               | Password   |
|-------|---------------------|------------|
| Admin | admin@smartcart.com | Admin@123  |
| User  | john@example.com    | User@123   |

---

## 📁 Project Structure

```
smartcart-backend/
├── src/main/java/com/smartcart/
│   ├── config/          # Security, JWT, CORS, Swagger config
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA entities
│   ├── enums/           # Role, OrderStatus, PaymentStatus
│   ├── exception/       # Global exception handling
│   ├── repository/      # Spring Data JPA repositories
│   └── service/         # Business logic services
└── src/main/resources/
    └── application.properties

smartcart-frontend/
├── src/
│   ├── api/             # Axios config + API service modules
│   ├── components/      # Header, Footer, ProductCard, Shared
│   ├── context/         # AuthContext, CartContext
│   └── pages/           # All page components
│       └── admin/       # Admin panel pages
├── index.html
├── tailwind.config.js
└── vite.config.js
>>>>>>> 8a60032c0a6a32520fb8a8ca5ab444043e8ac884
```

---

<<<<<<< HEAD
## 🚢 Production Deployment Checklist

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Must be `prod` | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC MySQL connection URL | `jdbc:mysql://host:3306/smartcart_db?useSSL=true` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `smartcart_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `(secure password)` |
| `REDIS_HOST` | Redis server hostname | `redis.example.com` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password (if auth enabled) | `(secure password)` |
| `JWT_SECRET` | **Must be unique** — app will fail to start with the default | Generate: `openssl rand -base64 64` |
| `RAZORPAY_KEY_ID` | Razorpay API key ID | `rzp_live_xxxxx` |
| `RAZORPAY_KEY_SECRET` | Razorpay API key secret | `(from Razorpay dashboard)` |
| `ALLOWED_ORIGINS` | Comma-separated list of allowed CORS origins | `https://yoursite.com` |

### Security Checklist

- [ ] Set a unique `JWT_SECRET` (app refuses to start with the default in prod)
- [ ] Set production `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`
- [ ] Configure `ALLOWED_ORIGINS` to only your frontend domain
- [ ] Ensure the database is initialized with `schema.sql` before first startup
- [ ] Verify `/actuator/health` returns `UP` without leaking internal details
- [ ] Confirm Swagger UI is not accessible (`springdoc` is disabled in prod)
- [ ] Confirm `/api/debug/*` endpoints are not accessible (guarded by `@Profile("dev")`)
- [ ] Deploy behind HTTPS (Render provides this automatically)

### Health Check Verification
```bash
# Should return {"status":"UP"} without internal details
curl https://your-backend-url/actuator/health
```

---

## 🛠 Active API Endpoints

Our endpoints are split into structural Swagger groups:

| Group | Endpoint | Method | Role | Description |
|---|---|---|---|---|
| **1. Auth** | `/api/auth/register` | `POST` | Public | Registers user and assigns USER role. |
| **1. Auth** | `/api/auth/login` | `POST` | Public | Returns stateful JWT token. |
| **2. Products** | `/api/products` | `GET` | Public | Paged & cached product list. |
| **2. Products** | `/api/products/{id}` | `GET` | Public | Cached product details. |
| **3. Cart** | `/api/cart` | `GET` | User | Fetch user's cart state. |
| **3. Cart** | `/api/cart/add` | `POST` | User | Add products to cart. |
| **4. Orders** | `/api/orders` | `POST` | User | Place order and reserve stock. |
| **5. Payments** | `/api/payments/create-order` | `POST` | User | Create Razorpay payment order. |
| **5. Payments** | `/api/payments/verify` | `POST` | User | Verify payment and confirm order. |
| **6. Admin** | `/api/admin/dashboard` | `GET` | Admin | Multi-dimensional financial analytics. |

> 💡 **Rate Limiting:** Auth endpoints (`/api/auth/**`) are rate-limited to 10 requests per minute per IP address.

=======
## 🛠 API Endpoints

| Module     | Endpoint                    | Method | Auth   |
|------------|-----------------------------|--------|--------|
| Auth       | `/api/auth/register`        | POST   | Public |
| Auth       | `/api/auth/login`           | POST   | Public |
| Products   | `/api/products`             | GET    | Public |
| Products   | `/api/products/{id}`        | GET    | Public |
| Products   | `/api/products/search`      | GET    | Public |
| Products   | `/api/products/filter`      | GET    | Public |
| Categories | `/api/categories`           | GET    | Public |
| Cart       | `/api/cart`                 | GET    | User   |
| Cart       | `/api/cart/add`             | POST   | User   |
| Orders     | `/api/orders`               | GET/POST| User  |
| Users      | `/api/users/profile`        | GET/PUT| User   |
| Users      | `/api/users/addresses`      | CRUD   | User   |
| Admin      | `/api/admin/dashboard`      | GET    | Admin  |
| Admin      | `/api/admin/products`       | CRUD   | Admin  |
| Admin      | `/api/admin/orders`         | GET/PUT| Admin  |
| Admin      | `/api/admin/users`          | GET/PUT| Admin  |
| Admin      | `/api/admin/categories`     | CRUD   | Admin  |
>>>>>>> 8a60032c0a6a32520fb8a8ca5ab444043e8ac884
