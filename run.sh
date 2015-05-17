#!/bin/sh

java -Duser.timezone=UTC -jar target/budgetapp.jar server config/config-dev.yml
