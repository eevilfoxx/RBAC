package com.RBAC;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testGetCurrentDate() {
        String currentDate = DateUtils.getCurrentDate();

        assertNotNull(currentDate);
        assertTrue(currentDate.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testGetCurrentDateTime() {
        String currentDateTime = DateUtils.getCurrentDateTime();

        assertNotNull(currentDateTime);
        assertTrue(currentDateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-01, 2024-01-02, true",
            "2024-01-02, 2024-01-01, false",
            "2024-01-01, 2024-01-01, false"
    })
    void testIsBefore(String date1, String date2, boolean expected) {
        boolean result = DateUtils.isBefore(date1, date2);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-02, 2024-01-01, true",
            "2024-01-01, 2024-01-02, false",
            "2024-01-01, 2024-01-01, false"
    })
    void testIsAfter(String date1, String date2, boolean expected) {
        boolean result = DateUtils.isAfter(date1, date2);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-01, 2024-01-01, true",
            "2024-01-01, 2024-01-02, false"
    })
    void testIsEqual(String date1, String date2, boolean expected) {
        boolean result = DateUtils.isEqual(date1, date2);

        assertEquals(expected, result);
    }

    @Test
    void testAddDays() {
        String date = "2024-01-01";

        String result = DateUtils.addDays(date, 5);

        assertEquals("2024-01-06", result);
    }

    @Test
    void testSubtractDays() {
        String date = "2024-01-10";

        String result = DateUtils.subtractDays(date, 5);

        assertEquals("2024-01-05", result);
    }

    @Test
    void testFormatRelativeTime() {
        assertDoesNotThrow(() -> DateUtils.formatRelativeTime(DateUtils.getCurrentDate()));
    }

    @ParameterizedTest
    @CsvSource({
            "2024-01-01, true",
            "2024-13-01, false",
            "2024-01-32, false",
            "abcd-ef-gh, false",
            ", false"
    })
    void testIsValidDate(String date, boolean expected) {
        boolean result = DateUtils.isValidDate(date);

        assertEquals(expected, result);
    }

    @Test
    void testParseDate() {
        assertEquals("2024-01-15", DateUtils.parseDate("2024-01-15"));
        assertEquals("2024-01-15", DateUtils.parseDate("15.01.2024"));
        assertEquals("2024-01-15", DateUtils.parseDate("01/15/2024"));
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    void testGetStatusWithIcon() {
        assertNotNull(DateUtils.getStatusWithIcon(DateUtils.getCurrentDate()));
        assertNotNull(DateUtils.getStatusWithIcon(DateUtils.addDays(DateUtils.getCurrentDate(), 5)));
        assertNotNull(DateUtils.getStatusWithIcon(DateUtils.subtractDays(DateUtils.getCurrentDate(), 5)));
    }

    @Test
    void testGetDaysUntil() {
        String futureDate = DateUtils.addDays(DateUtils.getCurrentDate(), 10);

        long days = DateUtils.getDaysUntil(futureDate);

        assertEquals(10, days);
    }

    @Test
    void testGetDaysSince() {
        String pastDate = DateUtils.subtractDays(DateUtils.getCurrentDate(), 10);

        long days = DateUtils.getDaysSince(pastDate);

        assertEquals(10, days);
    }
}