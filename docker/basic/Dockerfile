FROM eclipse-temurin:21.0.6_7-jre-jammy
LABEL authors="CytonicMC"

# Set the working directory
WORKDIR /app
# OTEL stuff
ARG OTEL_PORT
ARG OTEL_HOST

ENV OTEL_PORT=${OTEL_PORT}
ENV OTEL_HOSTNAME=${OTEL_HOST}

# Copy cytosis Jar
ADD ../../build/libs/cytosis.jar .

ADD ../../build/libs/config.toml .

# Expose the port
EXPOSE 25567

# Run the java
CMD ["java", "-jar", "cytosis.jar"]