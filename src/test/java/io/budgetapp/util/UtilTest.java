package io.budgetapp.util;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void testIsoDate() throws Exception {
        assertEquals(Util.toDate(LocalDate.of(2014, 8, 15)), Util.toDate("2014-08-15"));
        assertEquals(Util.toDate(LocalDate.of(2016, 2, 29)), Util.toDate("2016-02-29"));
    }

    @Test
    public void testBetweenDate() throws Exception {
        assertTrue(Util.betweenDate(Util.toDate("2014-08-19"), Util.toDate("2014-08-19"), Util.toDate("2014-08-19")));
        assertTrue(Util.betweenDate(Util.toDate("2014-08-19"), Util.toDate("2014-08-19"), Util.toDate("2014-08-29")));
        assertFalse(Util.betweenDate(Util.toDate("2014-08-18"), Util.toDate("2014-08-19"), Util.toDate("2014-08-29")));
    }

    @Test
    public void testInMonth() throws Exception {
        assertTrue(Util.inMonth(Util.toDate("2014-08-19"), Util.toDate("2014-08-19")));
        assertTrue(Util.inMonth(Util.toDate("2014-08-19"), Util.toDate("2014-08-01")));
        assertTrue(Util.inMonth(Util.toDate("2014-08-19"), Util.toDate("2014-08-31")));
        assertFalse(Util.inMonth(Util.toDate("2014-09-01"), Util.toDate("2014-08-31")));
    }

    @Test
    public void testYesterday() {
        assertEquals(30, Util.yesterday(LocalDate.of(2014, 10, 1)));
        assertEquals(14, Util.yesterday(LocalDate.of(2014, 10, 15)));
        assertEquals(30, Util.yesterday(LocalDate.of(2014, 10, 31)));
    }

    @Test
    public void testLastWeek() {
        assertEquals(52, Util.lastWeek(LocalDate.of(2014, 1, 1)));
        assertEquals(53, Util.lastWeek(LocalDate.of(2014, 1, 7)));
        assertEquals(1, Util.lastWeek(LocalDate.of(2014, 1, 8)));
        assertEquals(52, Util.lastWeek(LocalDate.of(2014, 12, 31)));
    }

    @Test
    public void testLastMonth() {
        assertEquals(12, Util.lastMonth(LocalDate.of(2014, 1, 1)));
        assertEquals(12, Util.lastMonth(LocalDate.of(2014, 1, 31)));
        assertEquals(1, Util.lastMonth(LocalDate.of(2014, 2, 1)));
        assertEquals(11, Util.lastMonth(LocalDate.of(2014, 12, 31)));
    }
}