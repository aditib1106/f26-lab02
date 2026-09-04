# Milestone 1 — the property that finds the bug

## The property

Added to `src/test/java/edu/cmu/cs214/availability/AvailabilityProperties.java`:

- **`@Property everyMinuteIsBookedOnceXorFree`** — generates a business day plus a
  list of **non-overlapping** bookings (via the `@Provide nonOverlappingScenarios`
  generator), then checks every minute of `[dayStart, dayEnd)`: that minute must be
  inside **exactly one** booking, **or** inside a free slot the calculator reported —
  never both, never neither. Encoded as `bookedCount + freeCount == 1`.

The provided property `freeSlotsNeverOverlapABooking` only checks *soundness*
("nothing reported free is actually booked"). This new property adds *completeness*
("every free minute is actually reported"), which is the half the example suite and
the provided property both miss.

## It fails against the unmodified `AvailabilityCalculator`

Command:

```
mvn test -Dtest='AvailabilityProperties#everyMinuteIsBookedOnceXorFree'
```

Output (stack trace trimmed):

```
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running edu.cmu.cs214.availability.AvailabilityProperties
timestamp = 2026-09-03T20:57:43.777256756, AvailabilityProperties:everyMinuteIsBookedOnceXorFree =
  org.opentest4j.AssertionFailedError:
    minute 0 is inside 0 booking(s) and is not reported free; exactly one of {booked, free} must hold ==> expected: <1> but was: <0>

                              |-----------------------jqwik-----------------------
tries = 1                     | # of calls to property
checks = 1                    | # of not rejected calls
generation = RANDOMIZED       | parameters are randomly generated
after-failure = SAMPLE_FIRST  | try previously failed sample, then previous seed
when-fixed-seed = ALLOW       | fixing the random seed is allowed
edge-cases#mode = MIXIN       | edge cases are mixed in
edge-cases#total = 120        | # of all combined edge cases
edge-cases#tried = 0          | # of edge cases tried in current run
seed = 3855221671477105253    | random seed to reproduce generated values

Shrunk Sample (3 steps)
-----------------------
  arg0: Scenario[dayStart=0, dayEnd=1, bookings=[]]

Original Sample
---------------
  arg0: Scenario[dayStart=39, dayEnd=43, bookings=[]]

  Original Error
  --------------
  org.opentest4j.AssertionFailedError:
    minute 39 is inside 0 booking(s) and is not reported free; exactly one of {booked, free} must hold ==> expected: <1> but was: <0>

[ERROR]   AvailabilityProperties.everyMinuteIsBookedOnceXorFree:58 minute 0 is inside 0 booking(s) and is not reported free; exactly one of {booked, free} must hold ==> expected: <1> but was: <0>
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

The random seed changes per run, so the *original* sample differs each time. A
representative earlier run (seed `4857804428459324443`) shrank from a scenario with
a real booking:

```
Original Sample
  arg0: Scenario[dayStart=217, dayEnd=321, bookings=[TimeInterval[start=238, end=262]]]
  Original Error: minute 262 is inside 0 booking(s) and is not reported free ...
```

Every run shrinks to the **same minimal counterexample** below.

## Shrunk minimal failing sample

```
Scenario[dayStart=0, dayEnd=1, bookings=[]]
```

Plain terms: a **one-minute business day (say 00:00–00:01) with no bookings at all**.
Minute 0 is obviously free, so the property expects it to appear in the calculator's
output. It does not.

## Why this input breaks the calculator

`AvailabilityCalculator.freeSlots` (`src/main/java/.../AvailabilityCalculator.java:19-39`)
sweeps the sorted bookings and, for each one, emits the gap **in front of it**
(`[cursor, booking.start())`), then advances `cursor` past it. After the loop it does
`return free;` — there is **no final step emitting `[cursor, dayEnd)`**, the free time
*after the last booking*.

Trace of `freeSlots(0, 1, [])`:

| step | state |
|------|-------|
| clip loop | `bookings` empty → `clipped = []` |
| sort | no-op |
| init | `free = []`, `cursor = 0` |
| sweep loop | `clipped` empty → body never runs |
| `return free` | returns `[]` |

So the whole day `[0, 1)` is reported neither booked nor free. `bookedCount + freeCount
= 0 + 0 = 0`, not `1`. The property fails at minute 0.

The same missing tail affects any input whose last booking ends before `dayEnd`
(the `[217, 321)` / booking `[238, 262)` case above: `[262, 321)` is dropped). The
example suite in `AvailabilityCalculatorTest` never catches it because every one of
its non-empty-result cases deliberately has a booking ending exactly at `DAY_END`,
so `cursor` always reaches `dayEnd` before the loop exits.

**The calculator is left unmodified — the point of Milestone 1 is to watch the gate
go red first.** The fix is Milestone 2.
