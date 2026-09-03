# SmartCart

Full-stack e-commerce app I built to learn Spring Boot + React end to end. Uses JWT auth, Razorpay payments, Redis caching, and Docker for deployment.

**Live:** Deployed on Render (free tier so first load takes ~30s to spin up)

## Features
- **AI-Powered Search:** Natural language product search using Google Gemini API
- Browse/search/filter products with pagination
- JWT-based authentication with role-based access (USER / ADMIN)
- Full shopping cart → checkout → Razorpay payment flow
- Order management with cancellation & automated refunds
- Admin dashboard with analytics, product/category/user management
- Redis caching with automatic fallback if Redis is down
- Rate limiting on auth endpoints to prevent brute force
- Dockerized with docker-compose for local development

## Tech Stack
* **Backend:** Spring Boot 3, Spring Security, Spring Data JPA, MySQL, Redis, Razorpay SDK
* **Frontend:** React 18, Vite, Tailwind CSS, Axios, React Router
* **DevOps:** Docker, GitHub Actions CI, Render

## Project Structure
```
smartcart-backend/    → Spring Boot REST API
smartcart-frontend/   → React SPA
docker-compose.yml    → Full stack with MySQL + Redis
```

## Running Locally

### Database
1. MySQL running on port 3306
2. Create the database:
   ```sql
   CREATE DATABASE smartcart_db;
   ```
3. Default creds are `root`/`admin` — change in `application-dev.yml` if yours are different

### Backend
```bash
cd smartcart-backend
./mvnw spring-boot:run
```
Starts on http://localhost:8080. Swagger docs at http://localhost:8080/swagger-ui.html

### Frontend
```bash
cd smartcart-frontend
npm install
npm run dev
```
Runs on http://localhost:5173

### Docker (everything at once)
```bash
docker compose up --build
```
Frontend → http://localhost:3000, Backend → http://localhost:8080

### Redis (optional)
If Redis isn't running locally the app still works — it just falls back to in-memory caching and logs a warning.

## Known Issues & Future Work
- Search is basic LIKE queries, no fuzzy matching — would switch to Elasticsearch for a production app
- JWT stored in localStorage (XSS risk) — should migrate to httpOnly cookies
- Rate limiter is in-memory so it won't work properly if running multiple backend instances
- No email notifications yet for order status changes
- Product images are URLs only, no actual file upload to S3 in prod yet
- Order cleanup task runs every 10 min but doesn't verify with Razorpay API if payment was actually made

## What I Learned
- How Spring Security filter chain actually works (spent way too long debugging 403s)
- Transaction boundaries matter a lot — had a bug where stock was reserved but cart wasn't cleared because they were in separate transactions
- Optimistic locking with `@Version` is great until you realize all your existing rows have `null` version — had to write a migration for that
- Redis can go down and your app shouldn't crash — built a graceful fallback
- Razorpay's HMAC signature verification is straightforward once you read their docs, but the webhook vs callback distinction tripped me up initially
