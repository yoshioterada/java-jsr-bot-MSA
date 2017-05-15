FROM java:8-jdk-alpine
MAINTAINER Yoshio Terada

VOLUME /tmp

ADD ./target/java-jsr-bot-MSA-1.0-SNAPSHOT.jar /java-jsr-bot-MSA-1.0-SNAPSHOT.jar
RUN sh -c 'touch /java-jsr-bot-MSA-1.0-SNAPSHOT.jar'
ENV JAVA_OPTS=""

RUN chmod 755 /java-jsr-bot-MSA-1.0-SNAPSHOT.jar
EXPOSE 8080 8181
ENTRYPOINT java -jar java-jsr-bot-MSA-1.0-SNAPSHOT.jar --autoBindHttp --autoBindSsl 

