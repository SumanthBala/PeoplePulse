# PeoplePulse - Git Ready Package

This package contains the complete PeoplePulse source code currently assembled in the project workspace.

## Structure
- `backend/` - Spring Boot 3.4.5 + Java backend
- `frontend/` - React + Vite frontend
- `frontend/src/services/api.js` - REST API client
- `frontend/src/main.jsx` - portal and UI components
- `frontend/src/styles.css` - application styling and latest UI fixes
- `frontend/tests/` - Playwright UI tests

## Important
The H2 runtime database is intentionally excluded from source control through `.gitignore`.
Do not commit `backend/data/` or generated build artifacts.

## Run backend
```bash
cd backend
mvn spring-boot:run
```

## Run frontend
```bash
cd frontend
npm install
npm run dev
```

## Default application accounts
See the existing `DataLoader.java` and project documentation for the configured development accounts.
