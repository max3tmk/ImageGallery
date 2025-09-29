# Stage 1: Build React app
FROM node:22 AS build

WORKDIR /app

# Copy only package.json + package-lock.json first (for caching)
COPY package*.json ./

# Install dependencies (cached if package.json/lock unchanged)
RUN npm install

# Copy the rest of the frontend source
COPY . .

# Build the app
RUN npm run build


# Stage 2: Serve with Nginx
FROM nginx:1.27-alpine AS serve

# Copy built files from previous stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
