# SmartCart — E-Commerce Application

SmartCart is a full-stack e-commerce application. It includes a Spring Boot backend and a React (Vite) frontend.

## Technologies
* **Backend:** Spring Boot, Spring Security (JWT), Spring Data JPA, Hibernate, MySQL, Redis
* **Frontend:** React, Vite, Tailwind CSS, Axios, React Router

## Project Structure
* `smartcart-backend/`: The backend REST API codebase.
* `smartcart-frontend/`: The frontend client codebase.

## Getting Started

### Database Setup
1. Make sure MySQL is running locally on port `3306`.
2. Create the database for the application:
   ```sql
   CREATE DATABASE smartcart_db;
   ```
3. The default application development configuration expects database username `root` and password `admin`. If your local setup is different, update the values in `smartcart-backend/src/main/resources/application-dev.yml`.

### Caching (Optional)
This project uses Redis for caching product catalog and categories. By default, it expects Redis running on `localhost:6379`. 
* If Redis is not running, the application will fallback to database queries (warnings will be logged).

### Running the Backend
Navigate to the backend folder and run Spring Boot using Maven:
```bash
cd smartcart-backend
./mvnw spring-boot:run
```
The backend server starts on `http://localhost:8080`.
The Swagger API documentation is available at `http://localhost:8080/swagger-ui.html` when running in the development profile.

### Running the Frontend
Navigate to the frontend folder, install dependencies, and start the development server:
```bash
cd smartcart-frontend
npm install
npm run dev
```
The frontend application runs on `http://localhost:5173`.

### Running via Docker Compose
If you have Docker Desktop running, you can run the entire system (including MySQL and Redis services) in containers:
```bash
docker compose up --build
```
* **Frontend:** `http://localhost:3000`
* **Backend REST API:** `http://localhost:8080`
