# Deployment Guide

This guide covers two ways to run the Insurance Management System:

- **A. Local run** — for your project demo / viva (simplest, recommended).
- **B. Free cloud deployment** — to put the app live on the internet, at zero cost, with no credit card.

The stack is **React (frontend) + Spring Boot (backend) + MySQL (database)**.

---

## A. Run Locally

### Prerequisites
- **JDK 17+** (this machine has JDK 26)
- **MySQL 8** running on `localhost:3306`
- **Node.js 18+**

### Steps
1. **Start MySQL** — make sure the `MySQL80` service is running (`services.msc`).
2. **Start the backend** — double-click `backend\run-backend.bat`, wait for `Started BackendApplication`.
   - API: `http://localhost:8081/api` · Swagger: `http://localhost:8081/swagger-ui.html`
3. **Start the frontend** — in a terminal:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Open `http://localhost:5173`.
4. **Log in** — default admin: `admin@ims.com` / `Admin@123`.

> Both servers must be running at the same time. If login shows a *Network Error*, the backend isn't running — start it first.

### One-command run with Docker (optional)
If you install **Docker Desktop**, the whole stack (MySQL + backend + frontend) starts with:
```bash
docker compose up --build
```
Frontend → `http://localhost:5173`, backend → `http://localhost:8081`. No local MySQL/JDK/Node needed.

---

## B. Free Cloud Deployment (no credit card)

We deploy three pieces to three free services:

| Piece | Service | Free? |
|-------|---------|-------|
| MySQL database | **TiDB Cloud Serverless** (MySQL-compatible) | Yes, no card |
| Spring Boot backend | **Render** (Docker web service) | Yes, no card |
| React frontend | **Vercel** | Yes, no card |

> You must create the accounts yourself (sign in with your GitHub account for the quickest setup). All required config files are already in this repo: `render.yaml`, `backend/Dockerfile`, `frontend/vercel.json`, `frontend/Dockerfile`.

### Step 1 — Create a free MySQL database (TiDB Cloud Serverless)
1. Go to **tidbcloud.com** → sign up (GitHub login works) → create a **Serverless** cluster (free).
2. Open the cluster → **Connect** → choose **Connect With: General / JDBC**. Note the **host, port (4000), user, and password**, and click **Generate/Set password**.
3. Create the database: in the cluster's SQL editor run `CREATE DATABASE insurance_db;`
4. Your JDBC URL will look like:
   ```
   jdbc:mysql://<host>:4000/insurance_db?sslMode=REQUIRE&serverTimezone=UTC
   ```

### Step 2 — Deploy the backend (Render)
1. Go to **render.com** → sign up with GitHub.
2. **New → Blueprint** → select your `insurance-management-system` repo. Render reads `render.yaml`.
3. When prompted, fill the environment variables:
   - `SPRING_DATASOURCE_URL` = the JDBC URL from Step 1
   - `DB_USERNAME` = your TiDB user
   - `DB_PASSWORD` = your TiDB password
   - `APP_CORS_ALLOWED_ORIGINS` = leave blank for now (fill after Step 3)
   - `APP_JWT_SECRET` = click **Generate**
4. Click **Apply**. The first build takes ~5–8 minutes. When done you get a URL like
   `https://ims-backend.onrender.com`. Test it: open `https://ims-backend.onrender.com/swagger-ui.html`.

> The free Render service **sleeps after 15 minutes** of inactivity and takes ~30–50s to wake on the next request. That's normal for the free tier.

### Step 3 — Deploy the frontend (Vercel)
1. Go to **vercel.com** → sign up with GitHub → **Add New → Project** → import your repo.
2. Set **Root Directory** to `frontend`. Vercel auto-detects Vite (build `npm run build`, output `dist`).
3. Add an **Environment Variable**:
   - `VITE_API_BASE` = `https://ims-backend.onrender.com/api` (your Render URL + `/api`)
4. Click **Deploy**. You get a URL like `https://ims-frontend.vercel.app`.

### Step 4 — Connect the two (CORS)
1. Back in **Render** → your backend service → **Environment** → set
   `APP_CORS_ALLOWED_ORIGINS` = `https://ims-frontend.vercel.app` (your Vercel URL, no trailing slash).
2. Save — Render redeploys automatically.
3. Open your Vercel URL and log in with `admin@ims.com` / `Admin@123`. 🎉

### Notes
- The admin account is seeded automatically on first backend start.
- To change the admin password, log in and use **Profile → Change Password**.
- Never commit real database passwords. They live only in the Render/Vercel dashboards.
