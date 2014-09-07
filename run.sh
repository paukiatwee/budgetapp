#!/bin/sh

java -Duser.timezone=UTC -jar target/finance-1.0.0-SNAPSHOT.jar server config-dev.yml
