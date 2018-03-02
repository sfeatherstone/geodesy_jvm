package uk.co.wedgetech.geodesy

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class DmsTest {

    @Before
    fun setup() {
    }

    @Test
    fun parseDMS() {
        assertEquals(51.4778, "51° 28′ 40.12″ N".parseDegreesMinutesSeconds(), 0.0001)
        assertEquals(-0.0015, "000° 00′ 05.31″ W".parseDegreesMinutesSeconds(), 0.0001)
        assertEquals(-51.4778, "51° 28′ 40.12″ S".parseDegreesMinutesSeconds(), 0.0001)
        assertEquals(0.0015, "000° 00′ 05.31″ E".parseDegreesMinutesSeconds(), 0.0001)
        assertEquals(-51.4778, "-51° 28′ 40.12″ ".parseDegreesMinutesSeconds(), 0.0001)
        assertEquals(-0.0015, "-000° 00′ 05.31″ ".parseDegreesMinutesSeconds(), 0.0001)
    }

    @Test
    fun toDMS() {
        assertEquals("005.01°", (365.007).toDMS(format = "d", dp = 2))
        assertEquals("005.01°", (-365.007).toDMS(format = "d", dp = 2))
    }

    @Test
    fun toDegrees() {
        assertEquals("350.01°", uk.co.wedgetech.geodesy.toDegrees(350.007, dp = 2))
        assertEquals("050.00°", uk.co.wedgetech.geodesy.toDegrees(50.0003, dp = 2))
        assertEquals("001.00°", uk.co.wedgetech.geodesy.toDegrees(1.0003, dp = 2))
        assertEquals("350.00°", uk.co.wedgetech.geodesy.toDegrees(350.0003, dp = 2))

        assertEquals("350°", uk.co.wedgetech.geodesy.toDegrees(350.007, dp = 0))
        assertEquals("351°", uk.co.wedgetech.geodesy.toDegrees(350.7, dp = 0))
        assertEquals("050°", uk.co.wedgetech.geodesy.toDegrees(50.007, dp = 0))
        assertEquals("001°", uk.co.wedgetech.geodesy.toDegrees(1.007, dp = 0))

        assertEquals("350.0001°", uk.co.wedgetech.geodesy.toDegrees(350.00007, 4))
        assertEquals("350.0000°", uk.co.wedgetech.geodesy.toDegrees(350.00004, 4))
        assertEquals("050.0070°", uk.co.wedgetech.geodesy.toDegrees(50.007, 4))
        assertEquals("001.0071°", uk.co.wedgetech.geodesy.toDegrees(1.00707, 4))
        assertEquals("037°", uk.co.wedgetech.geodesy.toDegrees(36.99999, 0))
        assertEquals("037.00°", uk.co.wedgetech.geodesy.toDegrees(36.99999, 2))
        assertEquals("036.99°", uk.co.wedgetech.geodesy.toDegrees(36.99, 2))
    }

    @Test
    fun toDegreesMinutesSeconds() {
        assertEquals("051°28′40″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(51.4778, 0, ""))
        assertEquals("051°X28′X40″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(51.4778, 0, "X"))
        assertEquals("006°X00′X01″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(6.0003, 0, "X"))
        assertEquals("006°X00′X01.08″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(6.0003, 2, "X"))
        assertEquals("051°X28′X40″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(51.4778, 0, "X"))
        assertEquals("051°X28′X40.08″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(51.4778, 2, "X"))
        assertEquals("350°X00′X01.08″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(350.0003, 2, "X"))

        assertEquals("036°X59′X59.96″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(36.99999, 2, "X"))
        assertEquals("037°X00′X00.0″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(36.99999, 1, "X"))
        assertEquals("037°X00′X00″", uk.co.wedgetech.geodesy.toDegreesMinutesSeconds(36.99999, 0, "X"))

    }
}