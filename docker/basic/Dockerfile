FROM eclipse-temurin:21.0.5_11-jre-jammy
LABEL authors="CytonicMC"

# Set the working directory
WORKDIR /app

# Copy cytosis Jar
ADD ../../build/libs/cytosis.jar .

ADD ../../build/libs/config.toml .

# Expose the port
EXPOSE 25567

# Run the java
CMD ["java", "-jar", "cytosis.jar"]