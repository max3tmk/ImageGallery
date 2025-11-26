FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --ignore-scripts
COPY . .
RUN npm rebuild --ignore-scripts && npm run build
FROM nginx:1.27-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
RUN chown -R appuser:appgroup /usr/share/nginx/html && \
    chown -R appuser:appgroup /var/cache/nginx && \
    chown -R appuser:appgroup /var/log/nginx && \
    chown -R appuser:appgroup /etc/nginx/conf.d
USER appuser
EXPOSE 80
CMD ["nginx", "-g", "daemon off; pid /tmp/nginx.pid;"]