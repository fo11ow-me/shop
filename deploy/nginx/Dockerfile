FROM nginx:alpine
COPY mall-admin/dist /usr/share/nginx/html/admin
COPY mall-portal/dist /usr/share/nginx/html/portal
COPY deploy/nginx/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
