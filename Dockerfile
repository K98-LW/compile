FROM gradle:jdk14
WORKDIR /app
COPY src /app/src
RUN gradle fatjar --no-daemon