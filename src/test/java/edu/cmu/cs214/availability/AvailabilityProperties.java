package edu.cmu.cs214.availability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Property-based tests for {@link AvailabilityCalculator}.
 *
 * <p>One example property is provided below: it checks that no returned free slot
 * overlaps a booking, and it passes. In Milestone 1 you add a stronger property
 * that pins down what "correct availability" actually means. See the lab handout.
 */
class AvailabilityProperties {

    private final AvailabilityCalculator calc = new AvailabilityCalculator();

    /** Provided example: every returned free slot is genuinely free (overlaps no booking). */
    @Property
    void freeSlotsNeverOverlapABooking(@ForAll("scenarios") Scenario s) {
        List<TimeInterval> free = calc.freeSlots(s.dayStart(), s.dayEnd(), s.bookings());
        for (TimeInterval slot : free) {
            for (TimeInterval booking : s.bookings()) {
                assertFalse(slot.overlaps(booking),
                    () -> "free slot " + slot + " overlaps booking " + booking);
            }
        }
    }

    // --- Milestone 1: stronger property ---

    /**
     * Every minute of the business day {@code [dayStart, dayEnd)} is accounted for
     * exactly once: it is either inside exactly one generated booking, or inside a
     * free slot the calculator reported — never both, and never neither.
     *
     * <p>Bookings here are generated non-overlapping, so "exactly one booking"
     * collapses to "at least one booking". The check {@code bookedCount + free == 1}
     * still catches a double-covered minute (sum 2) if the generator ever slipped.
     */
    @Property
    void everyMinuteIsBookedOnceXorFree(@ForAll("nonOverlappingScenarios") Scenario s) {
        List<TimeInterval> free = calc.freeSlots(s.dayStart(), s.dayEnd(), s.bookings());

        for (int minute = s.dayStart(); minute < s.dayEnd(); minute++) {
            int bookedCount = countCovering(s.bookings(), minute);
            int freeCount = anyCovers(free, minute) ? 1 : 0;

            final int m = minute;
            assertEquals(1, bookedCount + freeCount,
                () -> "minute " + m + " is inside " + countCovering(s.bookings(), m)
                    + " booking(s) and " + (anyCovers(free, m) ? "is" : "is not")
                    + " reported free; exactly one of {booked, free} must hold");
        }
    }

    /** How many of {@code intervals} contain {@code minute} (half-open [start, end)). */
    private static int countCovering(List<TimeInterval> intervals, int minute) {
        int count = 0;
        for (TimeInterval i : intervals) {
            if (i.start() <= minute && minute < i.end()) {
                count++;
            }
        }
        return count;
    }

    private static boolean anyCovers(List<TimeInterval> intervals, int minute) {
        return countCovering(intervals, minute) > 0;
    }

    /**
     * Generates a business day plus a list of strictly non-overlapping, in-hours,
     * sorted bookings. A list of deltas is read alternately as "free gap" then
     * "booking length" while walking from {@code dayStart} to {@code dayEnd}, so
     * consecutive bookings never touch or overlap and a trailing free gap is common.
     */
    @Provide
    Arbitrary<Scenario> nonOverlappingScenarios() {
        Arbitrary<Integer> dayStart = Arbitraries.integers().between(0, 800);
        Arbitrary<Integer> daySpan = Arbitraries.integers().between(1, 600);
        Arbitrary<List<Integer>> deltas =
            Arbitraries.integers().between(0, 90).list().ofMaxSize(12);

        return Combinators.combine(dayStart, daySpan, deltas).as((start, span, ds) -> {
            int end = start + span;
            List<TimeInterval> bookings = new ArrayList<>();
            int cursor = start;
            boolean nextIsBooking = false;
            for (int d : ds) {
                if (cursor >= end) {
                    break;
                }
                if (nextIsBooking) {
                    int bEnd = Math.min(cursor + d + 1, end); // +1 keeps the booking non-empty
                    bookings.add(new TimeInterval(cursor, bEnd));
                    cursor = bEnd;
                } else {
                    cursor += d; // free gap, may be zero
                }
                nextIsBooking = !nextIsBooking;
            }
            return new Scenario(start, end, bookings);
        });
    }

    /** Generates a business day plus a list of bookings (possibly unsorted, overlapping, or outside hours). */
    @Provide
    Arbitrary<Scenario> scenarios() {
        Arbitrary<Integer> minutes = Arbitraries.integers().between(0, 1440);
        Arbitrary<TimeInterval> intervals = Combinators.combine(minutes, minutes)
            .as((a, b) -> new TimeInterval(Math.min(a, b), Math.max(a, b) + 1));
        Arbitrary<List<TimeInterval>> bookings = intervals.list().ofMaxSize(6);
        return Combinators.combine(minutes, minutes, bookings)
            .as((a, b, bk) -> new Scenario(Math.min(a, b), Math.max(a, b) + 1, bk));
    }

    record Scenario(int dayStart, int dayEnd, List<TimeInterval> bookings) {
    }
}
