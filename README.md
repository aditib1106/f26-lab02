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

## Milestone 3: Test Suite Weaknesses

The original suite (`AvailabilityCalculatorTest` + the one provided property) ran with
~100% line/branch coverage on `AvailabilityCalculator.freeSlots`, all green. Coverage
didn't catch the bug because the bug is a *missing* statement — `freeSlots` never emits
the free interval after the last booking — and line/branch coverage only measures
whether code that exists got executed; it can't flag code that should exist and doesn't.

Three concrete weaknesses let it through:

1. **The provided property `freeSlotsNeverOverlapABooking` — observability gap.** It only
   checks that returned free slots don't overlap a booking, i.e. that the calculator never
   reports *too much* free time. It says nothing about reporting *too little*, which is
   exactly the bug. The buggy path ran on hundreds of generated scenarios; the assertion
   had no way to notice a missing interval.
2. **The example test `returnedSlotsNeverOverlapABooking` — observability gap.** Its input
   (one booking, 10:00–11:00, in a 9–17 day) actually triggers the bug — the calculator
   drops the correct 11:00–17:00 free slot. But the test only re-checks the same
   non-overlap property on whatever came back, so it stayed green on top of the bug.
3. **Every exact-output test in `AvailabilityCalculatorTest` — controllability gap.** Each
   one is built so the last booking ends exactly at closing time, so the calculator's
   internal cursor always reaches `dayEnd` before the missing trailing-gap code would ever
   be needed. The `assertEquals` checks were strong enough to catch a dropped interval;
   they were just never given an input that produces one.

Full writeup with the property code, jqwik's shrunk failing sample, and CI evidence is in
`MILESTONE1.md`.

## Where things are

- Component: `src/main/java/edu/cmu/cs214/availability/`
- Example-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityCalculatorTest.java`
- Property-based tests: `src/test/java/edu/cmu/cs214/availability/AvailabilityProperties.java`
- Setup: `SETUP.md`

See the Lab 2 handout on the course page for the three milestones you show a TA.
