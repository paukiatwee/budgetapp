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
}