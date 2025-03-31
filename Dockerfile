# Use OpenJDK 21 runtime as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Java source code into the container
COPY . /app

# Compile the Java code
RUN javac Server.java

# Expose the port your application runs on
EXPOSE 8020

# Run the server
CMD ["java", "Server"]
