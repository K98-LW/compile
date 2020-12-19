FROM openjdk:8
WORKDIR /app
COPY src /app/src
RUN cd src
RUN javac -encoding utf8 Main.java