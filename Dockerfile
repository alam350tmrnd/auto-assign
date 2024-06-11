# TMForce java container with externalized config and log

# Use an openjdk runtime as a parent image
FROM openjdk:8-jdk
ARG APPVERSION
ARG APPNAME
ARG RUNJAR=/app/${APPNAME}.jar
ENV CMDJAR ${RUNJAR}
# Set the working directory to /app
WORKDIR /app

# Copy jar into the container at /app
COPY target/${APPNAME}-${APPVERSION}.jar ${RUNJAR}
COPY config/config.yml /app/config.yml
COPY target/version.txt /app/version.txt
# Make port  available to the world outside this container
EXPOSE 80 4280

# Run app when the container launches
CMD java -jar -Duser.timezone=Asia/Kuala_Lumpur $CMDJAR server /app/config.yml