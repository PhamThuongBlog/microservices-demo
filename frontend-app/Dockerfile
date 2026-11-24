# Giai đoạn 1: Build source React thành HTML/CSS/JS
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Giai đoạn 2: Chạy bằng Nginx trên cổng 18080
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
RUN sed -i 's/listen       80;/listen       18080;/g' /etc/nginx/conf.d/default.conf
EXPOSE 18080
CMD ["nginx", "-g", "daemon off;"]