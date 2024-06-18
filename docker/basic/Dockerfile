FROM eclipse-temurin:21-jre-jammy
LABEL authors="CytonicMC"

# Set the working directory
WORKDIR /app

# Copy cytosis Jar
COPY ../../build/libs/cytosis.jar .

# Expose the port
EXPOSE 25565

# Run the java
CMD ["java", "-DSERVER_PORT=25565", "-jar", "--enable-preview", "cytosis.jar"]