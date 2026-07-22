# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre

# Install Tesseract OCR + English language data (Linux, since Render runs Linux containers)
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render provides the PORT env var at runtime; Spring Boot reads it via server.port
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
