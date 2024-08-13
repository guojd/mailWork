FROM openjdk:8-jdk-alpine


#ZCM
COPY ./target/mailWork-0.0.1-SNAPSHOT.jar /root/

EXPOSE 8082

ENTRYPOINT ["java","-jar","/root/mailWork-0.0.1-SNAPSHOT.jar"]