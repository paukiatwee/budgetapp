## Budget App

Budget App is open source personal budgeting application. Demo app is available [here](https://simple-finance.herokuapp.com/).

### Getting Started

#### Prerequisite
* JDK 8
* Postgresql 9.x
* Maven 3.x

#### Running Locally

+ Clone this repository.
+ Run `mvn package` to package budget app as single fat jar.
+ Run budget app using `run.sh`
+ Browse to [localhost:8080][]

### Configuration
Configuration file separate by environment:

* `config-dev.yml` - Local Development
* `src/test/resources/config-test.yml` - Integration Test
* `config-heroku.yml` - [Heroku][]
* `config-openshift.yml` - [OpenShift][] (WIP since JDK 8 is not support yet)


#### Database Configuration

**Supported Datanase**

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


### License

This code is released under version 2.0 of the [Apache License][].

[Heroku]: https://www.heroku.com
[Openshift]: https://www.openshift.com/
[localhost:8080]: http://localhost:8080
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
