package uk.co.wedgetech.geodesy

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class DmsTest {

    @Before
    fun setup() {
        Dms.separator = ""
    }

    @Test
    fun parseDMS() {
        assertEquals(51.4778, Dms.parseDMS("51° 28′ 40.12″ N"), 0.0001)
        assertEquals(-0.0015, Dms.parseDMS("000° 00′ 05.31″ W"), 0.0001)
        assertEquals(-51.4778, Dms.parseDMS("51° 28′ 40.12″ S"), 0.0001)
        assertEquals(0.0015, Dms.parseDMS("000° 00′ 05.31″ E"), 0.0001)
        assertEquals(-51.4778, Dms.parseDMS("-51° 28′ 40.12″ "), 0.0001)
        assertEquals(-0.0015, Dms.parseDMS("-000° 00′ 05.31″ "), 0.0001)
    }

    @Test
    fun toDMS() {
        assertEquals("005.01°", Dms.toDMS(365.007, format = "d", dp = 2))
        assertEquals("005.01°", Dms.toDMS(-365.007, format = "d", dp = 2))
    }

    @Test
    fun toDegrees() {
        assertEquals("350.01°", Dms.toDegrees(350.007, dp = 2))
        assertEquals("050.00°", Dms.toDegrees(50.0003, dp = 2))
        assertEquals("001.00°", Dms.toDegrees(1.0003, dp = 2))
        assertEquals("350.00°", Dms.toDegrees(350.0003, dp = 2))

        assertEquals("350°", Dms.toDegrees(350.007, dp = 0))
        assertEquals("351°", Dms.toDegrees(350.7, dp = 0))
        assertEquals("050°", Dms.toDegrees(50.007, dp = 0))
        assertEquals("001°", Dms.toDegrees(1.007, dp = 0))

        assertEquals("350.0001°", Dms.toDegrees(350.00007, 4))
        assertEquals("350.0000°", Dms.toDegrees(350.00004, 4))
        assertEquals("050.0070°", Dms.toDegrees(50.007, 4))
        assertEquals("001.0071°", Dms.toDegrees(1.00707, 4))
        assertEquals("037°", Dms.toDegrees(36.99999, 0))
        assertEquals("037.00°", Dms.toDegrees(36.99999, 2))
        assertEquals("036.99°", Dms.toDegrees(36.99, 2))
    }

    @Test
    fun toDegreesMinutesSeconds() {
        assertEquals("051°28′40″", Dms.toDegreesMinutesSeconds(51.4778, 0))
        Dms.separator = "X"
        assertEquals("051°X28′X40″", Dms.toDegreesMinutesSeconds(51.4778, 0))
        assertEquals("006°X00′X01″", Dms.toDegreesMinutesSeconds(6.0003, 0))
        assertEquals("006°X00′X01.08″", Dms.toDegreesMinutesSeconds(6.0003, dp = 2))
        assertEquals("051°X28′X40″", Dms.toDegreesMinutesSeconds(51.4778, 0))
        assertEquals("051°X28′X40.08″", Dms.toDegreesMinutesSeconds(51.4778, dp = 2))
        assertEquals("350°X00′X01.08″", Dms.toDegreesMinutesSeconds(350.0003, dp = 2))

        assertEquals("036°X59′X59.96″", Dms.toDegreesMinutesSeconds(36.99999, dp = 2))
        assertEquals("037°X00′X00.0″", Dms.toDegreesMinutesSeconds(36.99999, dp = 1))
        assertEquals("037°X00′X00″", Dms.toDegreesMinutesSeconds(36.99999, dp = 0))

    }
}