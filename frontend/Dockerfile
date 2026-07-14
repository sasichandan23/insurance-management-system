# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Inject the backend API base URL at build time (see src/api/client.js)
ARG VITE_API_BASE=http://localhost:8081/api
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

# ---- Run stage (static nginx) ----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
