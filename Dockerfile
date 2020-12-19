FROM openjdk:8
WORKDIR /app
COPY ./src/* /app/src/
RUN javac -encoding utf8 ./src/Main.java