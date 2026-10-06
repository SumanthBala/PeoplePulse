# PeoplePulse deployment: Netlify + Render

## 1. Backend first

Deploy the `backend` folder to Render as a Web Service.

Recommended settings:
- Root Directory: `backend`
- Runtime: Java
- Build Command: `./mvnw clean package -DskipTests`
- Start Command: `java -jar target/*.jar`

If the repository does not contain `mvnw`, use:
`mvn clean package -DskipTests`

Backend environment variables:
- `FRONTEND_URL=https://employeepls.netlify.app`
- `GEMINI_API_KEY=<your Gemini key>` (optional, only for AI)
- `GEMINI_MODEL=gemini-3.5-flash`

After deployment, copy the Render URL, for example:
`https://peoplepulse-backend.onrender.com`

Test:
`https://YOUR-BACKEND-URL/api/projects`

It should return JSON rather than a Render error page.

## 2. Netlify frontend

Netlify site: `employeepls.netlify.app`

Environment variable:
- Key: `VITE_API_URL`
- Value: `https://YOUR-BACKEND-URL`

Do not add a trailing slash.

Build settings are already in `netlify.toml`:
- Base directory: `frontend`
- Build command: `npm run build`
- Publish directory: `dist`

After adding the variable, trigger a new deploy. Vite environment variables are injected at build time.

## 3. What was changed

- `frontend/src/services/api.js`: all API calls now use `VITE_API_URL`.
- Resume upload uses the backend URL.
- Profile photo upload uses the backend URL.
- `frontend/public/_redirects`: React SPA fallback.
- `netlify.toml`: Netlify build configuration.
- `backend/application.properties`: Render-compatible `${PORT:8080}`.
- `CorsConfig.java`: allows `https://employeepls.netlify.app` (or `FRONTEND_URL`).
- `render.yaml`: Render deployment template.

## 4. Important

Do NOT put database passwords or Gemini/OpenAI secrets in `VITE_*` variables. Anything with `VITE_` is exposed to the browser.

The current project uses file-based H2:
`jdbc:h2:file:./data/peoplepulse_db;AUTO_SERVER=TRUE`

That is suitable for a demo, but cloud storage must be persistent if you need the H2 data to survive backend restarts/redeployments. For production, PostgreSQL is recommended.

Do not delete the existing local `data` directory if you need the current local H2 records.
