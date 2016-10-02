FROM anapsix/alpine-java

ADD target/budgetapp.jar /app/budgetapp.jar

ADD config/config.yml /app/config.yml

WORKDIR /app

CMD ["java", "-jar", "budgetapp.jar", "server", "config.yml"]

EXPOSE 8080
