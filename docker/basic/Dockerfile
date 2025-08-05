FROM eclipse-temurin:21.0.8_9-jre-jammy
LABEL authors="CytonicMC"

# Set the working directory
WORKDIR /app
# OTEL stuff
ARG OTEL_PORT
ARG OTEL_HOST

#ENV OTEL_PORT=${OTEL_PORT}
#ENV OTEL_HOSTNAME=${OTEL_HOST}

# Copy cytosis Jar
ADD ../../build/libs/cytosis.jar .

# Conditionally copy config.json (if it exists at build time)
COPY ../../config.json* .

# Expose the port
EXPOSE 25567

# Run the java
CMD ["java", "-jar", "cytosis.jar"]