FROM openjdk:8
WORKDIR /app
COPY ./src /app/
RUN javac -encoding utf8 ./src/Main.java