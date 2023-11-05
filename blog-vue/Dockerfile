FROM nginx:stable-alpine3.17
RUN mkdir -p /usr/local/aurora-vue/blog/
RUN mkdir -p /usr/local/aurora-vue/admin/
COPY blog-blog/dist/ /usr/local/aurora-vue/blog/
COPY blog-admin/dist/ /usr/local/aurora-vue/admin/
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80
EXPOSE 443
