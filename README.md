# Lab 2 Starter: Availability Calculator

A small reservation component. Given a room's bookings and the day's business hours,
`AvailabilityCalculator.freeSlots` computes when the room is free. It is the code you
work in for Lab 2.

It ships with a generated test suite that passes, and a property-based test harness
(jqwik) with one example property. Everything is green. Your job in Lab 2 is to decide
whether green actually means correct.

**Read `ARCHITECTURE.md` before the code.**

## Build and test

```
mvn test
```

`mvn test` runs both files, the ordinary example-based tests (`AvailabilityCalculatorTest`)
and the property-based tests (`AvailabilityProperties`). A code-coverage report is written
to `target/site/jacoco/index.html`.

## Tools

| Tool | Version | Role |
| --- | --- | --- |
| JDK (Temurin) | 21 (`maven.compiler.release` = 21; CI runs Temurin 21) | Language / runtime |
| Apache Maven | 3.8+ | Build and test driver (`mvn test`) |
| JUnit Jupiter | 5.10.2 | Example-based unit tests (`AvailabilityCalculatorTest`) |
| jqwik | 1.8.4 | Property-based testing (`AvailabilityProperties`); random generation + shrinking |
| Maven Surefire plugin | 3.2.5 | Runs `*Test` and `*Properties` classes during the `test` phase |
| JaCoCo (`jacoco-maven-plugin`) | 0.8.12 | Line/branch code-coverage report at `target/site/jacoco/index.html` |
| GitHub Actions | `.github/workflows/ci.yml` | CI: runs `mvn -B test` on every push and pull request |

## Continuous integration

This repository has CI configured in `.github/workflows/ci.yml`. GitHub disables workflows on a
fresh fork, so enable them once on your fork (the handout shows where). After that, every
push runs `mvn test`. You will watch the gate go red when your new property finds the bug, then
green once you fix it.

## Where things are

- Component: `src/main/java/edu/cmu/cs214/availability/`
- Example-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityCalculatorTest.java`
- Property-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityProperties.java`
- Setup: `SETUP.md`

See the Lab 2 handout on the course page for the three milestones you show a TA.
