## Build command:
# docker build -t myuser/budgetapp .
## Run command: (Remove link line when not using postgres)
# docker run --name budgetapp \
    --link=budgetapp-postgres:postgres \
    -d -p 8080:8080 \
    -v /local/path/to/budgetapp/config-dev.yml:/usr/src/app/config/config-dev.yml \
     myuser/budgetapp
## (Optional) Run postgres container
# docker run --name budgetapp-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres
FROM maven:3-jdk-8-onbuild
CMD ["java", "-Duser.timezone=UTC", "-jar", "target/budgetapp.jar", "server", "config/config-dev.yml"]
