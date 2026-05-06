# Module 15 — Deploying the App to the Web

## Learning Objectives
- Build Angular for production and understand the output
- Configure environment-specific settings
- Deploy to Vercel, Netlify, and a Docker/Nginx container
- Set up GitHub Actions CI/CD pipeline
- Configure Nginx for SPA routing

---

## 15.1 Production Build

```bash
ng build --configuration=production
```

Output in `dist/ibm-ems-app/browser/`:

```
dist/ibm-ems-app/
└── browser/
    ├── index.html
    ├── main-[hash].js          ← your app code (minified + tree-shaken)
    ├── polyfills-[hash].js     ← browser polyfills
    ├── chunk-[hash].js         ← lazy-loaded route chunks
    ├── styles-[hash].css       ← global styles
    └── assets/                 ← static assets
```

**What the production build does:**
- Minifies and obfuscates JavaScript
- Tree-shakes unused code
- Enables Ahead-of-Time (AOT) compilation
- Produces content hashes for cache busting
- Inlines critical CSS
- Splits code per lazy route

---

## 15.2 Environment Configuration

Angular 21 uses `app.config.ts` + environment files.

```bash
ng generate environments
# Creates: src/environments/environment.ts
#          src/environments/environment.development.ts
```

```ts
// src/environments/environment.ts (production)
export const environment = {
  production:  true,
  apiBaseUrl:  'https://api.ibm-ems.com/v1',
  appVersion:  '2.0.0',
  featureFlags: {
    analytics: true,
    darkMode:  false,
  },
};
```

```ts
// src/environments/environment.development.ts
export const environment = {
  production:  false,
  apiBaseUrl:  'https://jsonplaceholder.typicode.com',
  appVersion:  '2.0.0-dev',
  featureFlags: {
    analytics: false,
    darkMode:  true,
  },
};
```

```json
// angular.json — file replacements per configuration
{
  "configurations": {
    "production": {
      "fileReplacements": [{
        "replace": "src/environments/environment.development.ts",
        "with":    "src/environments/environment.ts"
      }]
    }
  }
}
```

```ts
// Use in app
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiBaseUrl;
}
```

---

## 15.3 Multiple Build Configurations

```json
// angular.json
{
  "configurations": {
    "production": {
      "budgets": [
        { "type": "initial", "maximumWarning": "500kb", "maximumError": "1mb" },
        { "type": "anyComponentStyle", "maximumWarning": "4kb" }
      ],
      "outputHashing": "all",
      "optimization": true,
      "sourceMap": false
    },
    "staging": {
      "fileReplacements": [{
        "replace": "src/environments/environment.development.ts",
        "with":    "src/environments/environment.staging.ts"
      }],
      "optimization": true,
      "sourceMap": true
    },
    "development": {
      "optimization": false,
      "sourceMap":    true,
      "extractLicenses": false
    }
  }
}
```

```bash
ng build --configuration=staging
ng build --configuration=production
```

---

## 15.4 Preview the Production Build

```bash
npx http-server dist/ibm-ems-app/browser -p 8080
# or
npm install -g serve
serve dist/ibm-ems-app/browser -p 8080
```

Open http://localhost:8080 — test that routes work on refresh.

---

## 15.5 Deploy to Vercel

```bash
npm install -g vercel
vercel login
vercel --prod
```

**SPA routing fix** — without this, direct URLs like `/employees/1` give 404 on refresh:

```json
// vercel.json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ],
  "headers": [
    {
      "source": "/assets/(.*)",
      "headers": [{ "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }]
    }
  ]
}
```

**Auto-deploy from GitHub:**
1. Push code to GitHub
2. Import project at vercel.com
3. Set build command: `ng build --configuration=production`
4. Set output directory: `dist/ibm-ems-app/browser`
5. Add environment variables (API URL, etc.)

---

## 15.6 Deploy to Netlify

```bash
# Install Netlify CLI
npm install -g netlify-cli
netlify login

# Build and deploy
ng build --configuration=production
netlify deploy --prod --dir dist/ibm-ems-app/browser
```

**SPA routing fix:**

```toml
# netlify.toml
[build]
  command     = "ng build --configuration=production"
  publish     = "dist/ibm-ems-app/browser"

[[redirects]]
  from   = "/*"
  to     = "/index.html"
  status = 200

[[headers]]
  for    = "/assets/*"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"
```

---

## 15.7 Docker + Nginx Deployment

### `Dockerfile`

```dockerfile
# Stage 1: Build Angular app
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci                          # install exact versions from package-lock.json

COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Serve with Nginx
FROM nginx:alpine AS server

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom nginx config
COPY nginx.conf /etc/nginx/conf.d/

# Copy built app from builder stage
COPY --from=builder /app/dist/ibm-ems-app/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### `nginx.conf`

```nginx
server {
    listen       80;
    server_name  _;
    root         /usr/share/nginx/html;
    index        index.html;

    # SPA — all routes serve index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets (hashed filenames)
    location ~* \.(js|css|png|svg|ico|woff2?)$ {
        expires    1y;
        add_header Cache-Control "public, immutable";
    }

    # Don't cache index.html
    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    # Security headers
    add_header X-Frame-Options        "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";
    add_header X-XSS-Protection       "1; mode=block";
    add_header Referrer-Policy        "strict-origin-when-cross-origin";
    add_header Content-Security-Policy
        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';";

    # Gzip compression
    gzip            on;
    gzip_vary       on;
    gzip_types      text/plain text/css application/json application/javascript;
    gzip_comp_level 6;
}
```

### `.dockerignore`

```
node_modules
dist
.git
.gitignore
*.md
coverage
.angular
```

### Build and run

```bash
# Build the Docker image
docker build -t ibm-ems-app:latest .

# Run locally
docker run -p 8080:80 ibm-ems-app:latest
# Open http://localhost:8080

# Push to registry
docker tag ibm-ems-app:latest your-registry.com/ibm-ems-app:v2.0.0
docker push your-registry.com/ibm-ems-app:v2.0.0
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'
services:
  ems-app:
    build: .
    ports:
      - "8080:80"
    environment:
      - NODE_ENV=production
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:80"]
      interval: 30s
      timeout:  10s
      retries:  3
```

---

## 15.8 GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
name: Build, Test & Deploy

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    name: Test
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run linter
        run: npm run lint

      - name: Run tests with coverage
        run: npm test -- --coverage --watchAll=false

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage
          path: coverage/

  build-and-deploy:
    name: Build & Deploy
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Build production
        run: npm run build -- --configuration=production
        env:
          VITE_API_URL: ${{ secrets.API_BASE_URL }}

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token:      ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id:     ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args:       '--prod'

  docker:
    name: Build & Push Docker Image
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ghcr.io/${{ github.repository }}:latest
            ghcr.io/${{ github.repository }}:${{ github.sha }}
```

---

## 15.9 Deployment Checklist

```
Build
  □ ng build --configuration=production runs without errors
  □ Bundle size within budget thresholds (angular.json budgets)
  □ All lazy routes split into separate chunks
  □ Source maps disabled (or served separately for error tracking)

Routing
  □ Direct URL access to /employees/1 works (returns 200, not 404)
  □ Nginx / Vercel / Netlify redirect config in place

Environment
  □ Production API URL configured
  □ No debug logging in production (check environment.production=true)
  □ No console.log left in production code

Security
  □ HTTPS enabled
  □ Security headers set (X-Frame-Options, CSP, etc.)
  □ API keys NOT in frontend bundle
  □ Auth tokens use httpOnly cookies or secure storage

Performance
  □ Static assets have long cache headers (js, css, images)
  □ index.html has no-cache header
  □ Gzip / Brotli compression enabled
  □ Lighthouse score > 90 for Performance

Monitoring
  □ Error tracking configured (Sentry, Datadog, etc.)
  □ Health check endpoint available
  □ Rollback plan in place
```

---

## Summary

| Platform | Config file | SPA fix |
|----------|-------------|---------|
| Vercel | `vercel.json` | `rewrites: [{ source: '/(.*)', destination: '/index.html' }]` |
| Netlify | `netlify.toml` | `[[redirects]] from="/*" to="/index.html" status=200` |
| Nginx | `nginx.conf` | `try_files $uri $uri/ /index.html` |
| Docker | `Dockerfile` | Uses Nginx config above |
| GitHub Actions | `.github/workflows/*.yml` | Runs build + deploy on push to main |

**Next → [Module 16: Angular Material (Bonus)](./16-angular-material-bonus.md)**
