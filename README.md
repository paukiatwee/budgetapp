# Budget App [![Build Status](https://travis-ci.org/paukiatwee/budgetapp.svg?branch=master)](https://travis-ci.org/paukiatwee/budgetapp)

Budget App is open source personal budgeting application.

### Demo is available [here](https://app.budgetapp.io/)

**Screenshot**

![](screenshot.png)


## Prerequisite

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [Maven 3](http://maven.apache.org/)

## Run Locally

+ Clone this repository.
+ Run `mvn package` to package budget app as single fat jar.
+ Run budget app using `run.sh`
+ Browse to [localhost:8080][]

## Configuration
Configuration file separate by environment:

* `config/config-dev.yml` - Local Development
* `src/test/resources/config-test.yml` - Integration Test
* `config/config-heroku.yml` - [Heroku][]
* `config/config-openshift.yml` - [OpenShift][] (WIP since JDK 8 is not support yet)


## Database Configuration

**Supported Database**

* PostgreSQL 9.x
* HSQL

**Code**
```
database:

  # the name of your JDBC driver
  driverClass: org.hsqldb.jdbcDriver

  # the username
  user: sa

  # the password
  password:

  # the JDBC URL
  url: jdbc:hsqldb:mem:test;sql.syntax_pgs=true
```


# License

This code is released under version 2.0 of the [Apache License][].

[Heroku]: https://www.heroku.com
[Openshift]: https://www.openshift.com/
[localhost:8080]: http://localhost:8080
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
