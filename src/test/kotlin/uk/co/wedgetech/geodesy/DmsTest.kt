package uk.co.wedgetech.geodesy

import org.junit.Test

import org.junit.Assert.*

class DmsTest {

    @Test
    fun parseDMS() {
        assertEquals(51.4778, Dms.parseDMS("51° 28′ 40.12″ N"), 0.0001)
        assertEquals(-0.0015, Dms.parseDMS("000° 00′ 05.31″ W"), 0.0001)
        assertEquals(-51.4778, Dms.parseDMS("51° 28′ 40.12″ S"), 0.0001)
        assertEquals(0.0015, Dms.parseDMS("000° 00′ 05.31″ E"), 0.0001)
        assertEquals(-51.4778, Dms.parseDMS("-51° 28′ 40.12″ "), 0.0001)
        assertEquals(-0.0015, Dms.parseDMS("-000° 00′ 05.31″ "), 0.0001)
    }
}