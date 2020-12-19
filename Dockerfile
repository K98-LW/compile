FROM openjdk:8
WORKDIR /app
COPY ./* /app/
RUN javac -encoding utf8 ./src/Main.java