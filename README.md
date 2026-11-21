# java-service

Build Software Factory fixture: a Spring Boot service with JUnit tests, built
with Maven, for the Java rule pack and for builds slow enough to test the step
time budget.

`GET /loans/overdue?on=2026-11-20` lists library loans overdue on a day, most
overdue first. "Overdue" is decided against a day passed in, never the clock,
so the rule is testable: a loan is not overdue on its due day.

## Build, test, run

```bash
mvn -B -q test
mvn -B -q spring-boot:run      # http://localhost:8080/loans/overdue
```
