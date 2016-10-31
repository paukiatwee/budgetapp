#!/bin/sh

mvn clean package

java -Duser.timezone=UTC -jar target/budgetapp.jar server config/config.yml
