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
        assertEquals(51.4778, "51° 28′ 40.12″ N".parseDegreesMinutesSeconds(), 0.00005)
        assertEquals(-0.0015, "000° 00′ 05.31″ W".parseDegreesMinutesSeconds(), 0.00005)
        assertEquals(-51.4778, "51° 28′ 40.12″ S".parseDegreesMinutesSeconds(), 0.00005)
        assertEquals(0.0015, "000° 00′ 05.31″ E".parseDegreesMinutesSeconds(), 0.00005)
        assertEquals(-51.4778, "-51° 28′ 40.12″ ".parseDegreesMinutesSeconds(), 0.00005)
        assertEquals(-0.0015, "-000° 00′ 05.31″ ".parseDegreesMinutesSeconds(), 0.00005)
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

    @Test
    fun test_ParseVariations() {
        val variations = arrayOf(
                "45.76260",
                "45.76260 ",
                "45.76260°",
                "45°45.756′",
                "45° 45.756′",
                "45 45.756",
                "45°45′45.36″",
                "45º45\"45.36\"",
                "45°45’45.36”",
                "45 45 45.36 ",
                "45° 45′ 45.36″",
                "45º 45\" 45.36\"",
                "45° 45’ 45.36”")

        for (v in variations)
            assertEquals("parse dms variations " + v, 45.76260, v.parseDegreesMinutesSeconds(), 0.00001);

        for (v in variations)
            assertEquals("parse dms variations " + "-" + v, -45.76260, ("-" + v).parseDegreesMinutesSeconds(), 0.00001)

        for (v in variations)
            assertEquals("parse dms variations " + v + "N", 45.76260, (v + "N").parseDegreesMinutesSeconds(), 0.00001)

        for (v in variations)
            assertEquals("parse dms variations " + v + "S", -45.76260, (v + "S").parseDegreesMinutesSeconds(), 0.00001)

        for (v in variations)
            assertEquals("parse dms variations " + v + "E", 45.76260, (v + "E").parseDegreesMinutesSeconds(), 0.00001)

        for (v in variations)
            assertEquals("parse dms variations " + v + "W", -45.76260, (v + "W").parseDegreesMinutesSeconds(), 0.00001)

        assertEquals("parse dms variations " + " ws before+after ", 45.76260, " 45°45′45.36″ ".parseDegreesMinutesSeconds(), 0.00001);

    }

    @Test
    fun test_compassPoint() {
        //TODO clean up
        assertEquals("1 -> N ", "N", compassPoint(1.0))
        assertEquals("0 -> N ", "N", compassPoint(0.0))
        assertEquals("-1 -> N ", "N", compassPoint(-1.0))
        assertEquals("359 -> N ", "N", compassPoint(359.0))
        assertEquals("24 -> NNE ", "NNE", compassPoint(24.0))
        assertEquals("24:1 -> N ", compassPoint(24.0, 1), "N")
        assertEquals("24:2 -> NE ", compassPoint(24.0, 2), "NE")
        assertEquals("24:3 -> NNE ", "NNE", compassPoint(24.0, 3))
        assertEquals("226 -> SW ", compassPoint(226.0), "SW")
        assertEquals("226:1 -> W ", compassPoint(226.0, 1), "W")
        assertEquals("226:2 -> SW ", compassPoint(226.0, 2), "SW")
        assertEquals("226:3 -> SW ", compassPoint(226.0, 3), "SW")
        assertEquals("237 -> WSW ", compassPoint(237.0), "WSW")
        assertEquals("237:1 -> W ", compassPoint(237.0, 1), "W")
        assertEquals("237:2 -> SW ", compassPoint(237.0, 2), "SW")
        assertEquals("237:3 -> WSW ", compassPoint(237.0, 3), "WSW")
    }

    @Test
    fun test_parseDegreesMinutesSeconds0() {
        assertEquals("parse 0.0°", 0.0, "0.0°".parseDegreesMinutesSeconds(), 0.00001)
        assertEquals("output 000.0000°", "000.0000°", (0.0).toDMS("d"))
        assertEquals("parse 0°", 0.0, "0°".parseDegreesMinutesSeconds(), 0.00001)
        assertEquals("output 000°", "000°", (0.0).toDMS("d", 0))
        assertEquals("parse 000 00 00 ", 0.0, "000 00 00 ".parseDegreesMinutesSeconds(), 0.00001)
        assertEquals("parse 000°00′00″", 0.0, "000°00′00″".parseDegreesMinutesSeconds(), 0.00001)
        assertEquals("output 000°00′00″", "000°00′00″", (0.0).toDMS())
        assertEquals("parse 000°00′00.0″", 0.0, "000°00′00.0″".parseDegreesMinutesSeconds(), 0.00001)
        assertEquals("output 000°00′00.00″", "000°00′00.00″", (0.0).toDMS("dms", 2))
    }

    @Test
    fun testOutOfRange() {
        assertEquals("parse 185", 185.0, "185".parseDegreesMinutesSeconds(), 0.001)
        assertEquals("parse 365", 365.0, "365".parseDegreesMinutesSeconds(), 0.001)
        assertEquals("parse -185", -185.0, "-185".parseDegreesMinutesSeconds(), 0.001)
        assertEquals("parse -365", -365.0, "-365".parseDegreesMinutesSeconds(), 0.001)
    }

    @Test
    fun testOutputVariations() {
        assertEquals("output dms ", "045°45′45″", 45.76260.toDMS())
        assertEquals("output dms " + "d", "045.7626°", 45.76260.toDMS("d"))
        assertEquals("output dms " + "dm", "045°45.76′", 45.76260.toDMS("dm"))
        assertEquals("output dms " + "dms", "045°45′45″", 45.76260.toDMS("dms"))
        assertEquals("output dms " + "dm,6", "045.762600°", 45.76260.toDMS("d", 6))
        assertEquals("output dms " + "dm,4", "045°45.7560′", 45.76260.toDMS("dm", 4))
        assertEquals("output dms " + "dms,2", "045°45′45.36″", 45.76260.toDMS("dms", 2))
        assertEquals("output dms " + "xxx", "045°45′45″", 45.76260.toDMS("xxx"))
        assertEquals("output dms " + "xxx,6", "045°45′45.360000″", 45.76260.toDMS("xxx", 6)) // !!
    }
/*
    describe("misc", function() {
        assertEquals("toLat num",    Dms.toLat(51.2, "dms"))"51°12′00″N")
        assertEquals("toLat str",    Dms.toLat("51.2", "dms"))"51°12′00″N")
        assertEquals("toLat xxx",    Dms.toLat("xxx", "dms"))"–")
        assertEquals("toLon num",    Dms.toLon(0.33, "dms"))"000°19′48″E")
        assertEquals("toLon str",    Dms.toLon("0.33", "dms"))"000°19′48″E")
        assertEquals("toLon xxx",    Dms.toLon("xxx", "dms"))"–")
        assertEquals("toDMS rnd-up", Dms.toDMS(51.19999999999999, "d"))"051.2000°")
        assertEquals("toDMS rnd-up", Dms.toDMS(51.19999999999999, "dm"))"051°12.00′")
        assertEquals("toDMS rnd-up", Dms.toDMS(51.19999999999999, "dms"))"051°12′00″")
        assertEquals("toBrng",       Dms.toBrng(1))"001°00′00″")
    });

    describe("parse failures", function() {
        assertEquals("parse 0 0 0 0", Dms.parseDMS("0 0 0 0").should.be.NaN
        assertEquals("parse xxx",     Dms.parseDMS("xxx").should.be.NaN
        assertEquals("parse """,      Dms.parseDMS("").should.be.NaN
        assertEquals("parse null",    Dms.parseDMS(null).should.be.NaN
        assertEquals("parse obj",     Dms.parseDMS({ a: 1 }).should.be.NaN
        assertEquals("parse true",    Dms.parseDMS(true).should.be.NaN
        assertEquals("parse false",   Dms.parseDMS(false).should.be.NaN
    });

    describe("convert failures", function() {
        assertEquals("output 0 0 0 0", should.equal(Dms.toDMS("0 0 0 0"), null)
        assertEquals("output xxx",     should.equal(Dms.toDMS("xxx", "dms", 2), null)
        assertEquals("output xxx",     should.equal(Dms.toDMS("xxx"), null)
        assertEquals("output """,      should.equal(Dms.toDMS(""), "000°00′00″") // TODO: fix on next semver major
        assertEquals("output null",    should.equal(Dms.toDMS(null), "000°00′00″") // TODO: fix on next semver major
        assertEquals("output obj",     should.equal(Dms.toDMS({ a: 1 }), null)
        assertEquals("output true",    should.equal(Dms.toDMS(true), "001°00′00″") // TODO: fix on next semver major
        assertEquals("output false",   should.equal(Dms.toDMS(false), "000°00′00″") // TODO: fix on next semver major
        assertEquals("output ∞",       should.equal(Dms.toDMS(1/0), "ity°aN′NaN″") // TODO: fix on next semver major
    });
*/
}