FROM openjdk:11
COPY out/artifacts/dropbridge_jar /usr/dropbridge
WORKDIR /usr/dropbridge
EXPOSE 8033
CMD ["java", "-jar", "dropbridge.jar"]
