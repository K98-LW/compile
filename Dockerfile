FROM gcc:10
WORKDIR /app/
COPY ./* ./
RUN gcc main.c -o program
RUN chmod +x program