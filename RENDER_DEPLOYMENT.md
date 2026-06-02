# SmartCart Full-Stack Deployment Guide on Render

This guide provides step-by-step instructions on how to deploy your entire **SmartCart** application (Database, Backend, and Frontend) on **Render**.

---

## 📋 Prerequisites
Before starting, ensure your local changes are pushed to a **GitHub repository** (either public or private), as Render deploys directly from your GitHub account.

---

## 🗄️ Step 1: Deploy the PostgreSQL Database
Render offers managed PostgreSQL databases with automated backups and maintenance.

1. Go to your [Render Dashboard](https://dashboard.render.com/) and click **New +** -> **PostgreSQL**.
2. Configure the database details:
   * **Name:** `smartcart-db`
   * **Database Name:** `smartcart_db`
   * **User:** `postgres`
   * **Region:** Select the region closest to your users.
   * **Instance Type:** Select **Free** (or a paid tier if preferred).
3. Click **Create Database**.
4. Once the status shows **Available**, copy the **Internal Database URL** (which we will use to connect your backend).
   * It will look like: `postgres://postgres:password@host.oregon-postgres.render.com/smartcart_db`

---

## ☕ Step 2: Deploy the Spring Boot Backend (Docker Service)
Because your backend folder contains a production-grade multi-stage `Dockerfile`, Render can build and run it securely in a container with a single click.

1. In the Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Configure the service details:
   * **Name:** `smartcart-backend`
   * **Region:** Select the same region as your database.
   * **Root Directory:** `smartcart-backend` *(Crucial: This tells Render where the backend sub-project resides)*
   * **Runtime:** **Docker** *(Render will auto-detect your Dockerfile)*
   * **Instance Type:** Select your plan (Free is supported).
4. Click **Advanced** and add the following **Environment Variables**:

| Variable | Value | Description |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | *[Paste your Internal Database URL]* | Connects your backend to your Render PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database Username |
| `SPRING_DATASOURCE_PASSWORD` | *[Your Database Password]* | Found in your Render PostgreSQL settings |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | **(Highly recommended for first deploy)** Automatically creates tables in PostgreSQL |
| `ALLOWED_ORIGINS` | `https://smartcart-shop.onrender.com` | *[Use your Frontend Static Site URL once deployed]* |
| `JWT_SECRET` | *[Generate a 256-bit secure key]* | Security key for encrypting authentication tokens |
| `RAZORPAY_KEY_ID` | `rzp_test_...` | Your Razorpay Merchant key id |
| `RAZORPAY_KEY_SECRET` | *[Your Razorpay Secret]* | Your Razorpay Merchant secret key |

5. Click **Create Web Service**. 

Once the deployment finishes successfully, copy your backend's public URL (e.g. `https://smartcart-backend.onrender.com`).

---

## ⚛️ Step 3: Deploy the React Frontend (Static Site)
Render hosts React Static Sites on a lightning-fast global CDN for free.

1. In the Render Dashboard, click **New +** -> **Static Site**.
2. Connect the same GitHub repository.
3. Configure the static site details:
   * **Name:** `smartcart-shop`
   * **Root Directory:** `smartcart-frontend` *(Crucial: Focuses on the React app)*
   * **Build Command:** `npm ci && npm run build`
   * **Publish Directory:** `dist`
4. Click **Advanced** and add the following **Environment Variable**:
   * `VITE_API_BASE_URL` = `https://smartcart-backend.onrender.com/api` *(Replace with your actual Backend URL from Step 2)*
5. **Set up Routing Rewrites (Crucial for React Router):**
   * Go to your Static Site's settings dashboard.
   * Under the **Redirects/Rewrites** section, click **Add Rule**.
   * **Source:** `/*`
   * **Destination:** `/index.html`
   * **Action:** `Rewrite` (Status `200`)
   * *This ensures refreshing secondary routes (e.g. `/cart` or `/login`) does not result in a 404 error.*
6. Click **Create Static Site**.

---

## 🎉 Verification
1. Once all three services are green, open your **Frontend URL** (e.g., `https://smartcart-shop.onrender.com`).
2. Register a new user, log in, browse the dynamically loaded products, add items to the cart, and test the checkout flow.
3. Confirm that the Spring Boot system successfully initialized the database and seeded the categories and products automatically!
