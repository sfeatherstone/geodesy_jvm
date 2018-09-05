package com.sfeatherstone.geodesy.model.spherical.vectors

import com.sfeatherstone.geodesy.*
import org.junit.Test
import org.junit.Assert.*

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/*  Geodesy Test Harness - latlon-vectors                             (c) Chris Veness 2014-2017  */
/*                                                                    (c) Simon Fratherstone 2018 */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

class VectorsTest {
    val R = 6371e3
    val π = Math.PI

    @Test
    fun formatingTest() {
        assertEquals("toString d", "51.521470°N, 000.138833°W", LatLon(51.521470, -0.138833).toString("d", 6))
        assertEquals("toString dms", "51°31′17.29″N, 000°08′19.80″W", LatLon(51.521470, -0.138833).toString("dms", 2))
    }

    @Test
    fun geodesicsTest() {
        val cambg = LatLon(52.205, 0.119)
        val paris = LatLon(48.857, 2.351)
        assertEquals("distance", 4.043e+5, cambg.distanceTo(paris).toPrecision(4), 0.00001)
        assertEquals("distance (miles)", 251.2, cambg.distanceTo(paris, 3959.0).toPrecision(4), 0.00001)
        assertEquals("initial bearing", 156.2, cambg.bearingTo(paris).toFixed(1), 0.0001)
        assertEquals("midpoint", "50.5363°N, 001.2746°E", cambg.midpointTo(paris).toString("d"))
        assertEquals("int.point", "51.3721°N, 000.7073°E", cambg.intermediatePointTo(paris, 0.25).toString("d"))
        assertEquals("int.point-chord", "51.3723°N, 000.7072°E", cambg.intermediatePointOnChordTo(paris, 0.25).toString("d"))

        var greenwich = LatLon(51.4778, -0.0015)
        val dist = 7794.0
        val brng = 300.7
        assertEquals("dest’n", "51.5135°N, 000.0983°W", greenwich.destinationPoint(dist, brng).toString("d"))
        assertEquals("dest’n inc r", "51.5135°N, 000.0983°W", greenwich.destinationPoint(dist, brng, 6371e3).toString("d"))

        var bradwell = LatLon(53.3206, -1.7297)
/*        assertEquals("cross-track",       -307.5, LatLon(53.2611, -0.7972).crossTrackDistanceTo(bradwell, LatLon(53.1887,  0.1334)).toPrecision(4), 0.001);
        assertEquals("along-track",       6.233e+4, LatLon(53.2611, -0.7972).alongTrackDistanceTo(bradwell, LatLon(53.1887,  0.1334)).toPrecision(4), 0.0001);

        assertEquals("cross-track NE",    -1.112e+5, LatLon(1.0, 1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track SE",    1.112e+5, LatLon(-1.0,  1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track SW?",   1.112e+5, LatLon(-1.0, -1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track NW?",   -1.112e+5, LatLon( 1.0, -1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)

        assertEquals("along-track NE",    1.112e+5, LatLon( 1.0,  1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track SE",    1.112e+5, LatLon(-1.0,  1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track SW",    -1.112e+5, LatLon(-1.0, -1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track NW",    -1.112e+5, LatLon( 1.0, -1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)

        assertEquals("cross-track brng w-e",  -1.112e+5, LatLon(1.0, 0.0).crossTrackDistanceTo(LatLon(0.0, 0.0), 90.0).toPrecision(4), 0.0001)
        assertEquals("cross-track brng e-w",  1.112e+5, LatLon(1.0, 0.0).crossTrackDistanceTo(LatLon(0.0, 0.0), 270.0).toPrecision(4), 0.0001)
*/
        assertEquals("nearest point on segment 1", "51.0004°N, 001.9000°E", LatLon(51.0, 1.9).nearestPointOnSegment(LatLon(51.0, 1.0), LatLon(51.0, 2.0)).toString("d"))
        assertEquals("nearest point on segment 1d", 42.71, LatLon(51.0, 1.9).nearestPointOnSegment(LatLon(51.0, 1.0), LatLon(51.0, 2.0)).distanceTo(LatLon(51.0, 1.9)).toPrecision(4), 0.0001)
        assertEquals("nearest point on segment 2", "51.0000°N, 002.0000°E", LatLon(51.0, 2.1).nearestPointOnSegment(LatLon(51.0, 1.0), LatLon(51.0, 2.0)).toString("d"))
        assertEquals("nearest point on segment JB", "00.0000°N, 020.0000°E", LatLon(10.0, -140.0).nearestPointOnSegment(LatLon(0.0, 20.0), LatLon(0.0, 40.0)).toString("d"))
    }

    @Test
    fun EdWilliamsTest() {
        var lax = LatLon("33° 57′N".parseDegreesMinutesSeconds(), "118° 24′W".parseDegreesMinutesSeconds())
        var jfk = LatLon("40° 38′N".parseDegreesMinutesSeconds(), "073° 47′W".parseDegreesMinutesSeconds())
        assertEquals("distance nm", 2144.0, lax.distanceTo(jfk, 180 * 60 / π).toPrecision(4), 0.0001)
        assertEquals("bearing", 66.0, lax.bearingTo(jfk).toPrecision(2), 0.001)
        assertEquals("intermediate", "34°37′N, 116°33′W", lax.intermediatePointTo(jfk, 100.0 / 2144.0).toString("dm", 0))
        var d = LatLon("34:30N".parseDegreesMinutesSeconds(), "116:30W".parseDegreesMinutesSeconds())
        //assertEquals("cross-track",    d.crossTrackDistanceTo(lax, jfk, 180*60/π).toPrecision(5).should.equal("7.4523"));
        assertEquals("intermediate", "38°40.167′N, 101°37.570′W", lax.intermediatePointTo(jfk, 0.4).toString("dm", 3))
        var reo = LatLon("42.600N".parseDegreesMinutesSeconds(), "117.866W".parseDegreesMinutesSeconds())
        var bke = LatLon("44.840N".parseDegreesMinutesSeconds(), "117.806W".parseDegreesMinutesSeconds())
        //assertEquals("intersection",   "43.572°N, 116.189°W", LatLon.intersection(reo, 51, bke, 137).toString("d", 3))
    }

    @Test
    fun IntersectionsTest() {
        var N = 0.0
        val E = 90.0
        val S = 180.0
        val W = 270.0
        /*   assertEquals("toward 1,1 N,E nearest",         "00.9998°N, 001.0000°E", LatLon.intersection(LatLon(0, 1), N, LatLon(1, 0), E).toString("d"));
           assertEquals("toward 1,1 E,N nearest",         "00.9998°N, 001.0000°E", LatLon.intersection(LatLon(1, 0), E, LatLon(0, 1), N).toString("d"));
           assertEquals("toward 1,1 N,E antipodal",       "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(2, 1), N, LatLon(1, 0), E).toString("d"));
           assertEquals("toward/away 1,1 N,W antipodal",  "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(0, 1), N, LatLon(1, 0), W).toString("d"));
           assertEquals("toward/away 1,1 W,N antipodal",  "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(1, 0), W, LatLon(0, 1), N).toString("d"));
           assertEquals("toward/away 1,1 S,E antipodal",  "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(0, 1), S, LatLon(1, 0), E).toString("d"));
           assertEquals("toward/away 1,1 E,S antipodal",  "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(1, 0), E, LatLon(0, 1), S).toString("d"));
           assertEquals("away 1,1 S,W antipodal",         "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(0, 1), S, LatLon(1, 0), W).toString("d"));
           assertEquals("away 1,1 W,S antipodal",         "00.9998°S, 179.0000°W", LatLon.intersection(LatLon(1, 0), W, LatLon(0, 1), S).toString("d"));

           assertEquals("1E/90E N,E antipodal",           "00.0175°S, 179.0000°W", LatLon.intersection(LatLon(0, 1), N, LatLon(1, 90), E).toString("d"));
           assertEquals("1E/90E N,E nearest",             "00.0175°N, 179.0000°W", LatLon.intersection(LatLon(0, 1), N, LatLon(1, 92), E).toString("d"));

           assertEquals("brng+end 1a",                    "01.0003°N, 002.0000°E", LatLon.intersection(LatLon(1, 0), LatLon(1, 3), LatLon(2, 2), S).toString("d"));
           assertEquals("brng+end 1b",                    "01.0003°N, 002.0000°E", LatLon.intersection(LatLon(2, 2), S, LatLon(1, 0), LatLon(1, 3)).toString("d"));
           assertEquals("brng+end 2a",                    "01.0003°S, 178.0000°W", LatLon.intersection(LatLon(1, 0), LatLon(1, 3), LatLon(2, 2), N).toString("d"));
           assertEquals("brng+end 2b",                    "01.0003°S, 178.0000°W", LatLon.intersection(LatLon(2, 2), N, LatLon(1, 0), LatLon(1, 3)).toString("d"));

           assertEquals("end+end",                        "02.4994°N, 002.5000°E", LatLon.intersection(LatLon(1, 1), LatLon(2, 2), LatLon(1, 4), LatLon(2, 3)).toString("d"));
       */
        val stn = LatLon(51.8853, 0.2545)
        val cdg = LatLon(49.0034, 2.5735)
        //  assertEquals("stn-cdg-bxl",                    "50.9078°N, 004.5084°E", LatLon.intersection(stn, 108.547, cdg, 32.435).toString("d"));
    }

    @Test
    fun PolygonalTest() {
        var polyTriangle = arrayOf(LatLon(1.0, 1.0), LatLon(2.0, 1.0),
                LatLon(1.0, 2.0))
        var polySquareCw = arrayOf(LatLon(1.0, 1.0), LatLon(2.0, 1.0),
                LatLon(2.0, 2.0), LatLon(1.0, 2.0))
        var polySquareCcw = arrayOf(LatLon(1.0, 1.0), LatLon(1.0, 2.0),
                LatLon(2.0, 2.0), LatLon(2.0, 1.0))
        var polyQuadrant = arrayOf(LatLon(0.0, 0.0), LatLon(0.0, 90.0),
                LatLon(90.0, 0.0))
        var polyHemi = arrayOf(LatLon(0.0, 1.0), LatLon(45.0, 0.0),
                LatLon(89.0, 90.0), LatLon(45.0, 180.0), LatLon(0.0, 179.0),
                LatLon(-45.0, 180.0), LatLon(-89.0, 90.0), LatLon(-45.0, 0.0))
        var polyGc = arrayOf(LatLon(10.0, 0.0), LatLon(10.0, 90.0),
                LatLon(0.0, 45.0))
        var polyPole = arrayOf(LatLon(89.0, 0.0), LatLon(89.0, 120.0),
                LatLon(89.0, -120.0))
        var polyPoleEdge = arrayOf(LatLon(85.0, 90.0), LatLon(85.0, 0.0), LatLon(85.0, -90.0))
        var polyConcave = arrayOf(LatLon(1.0, 1.0), LatLon(5.0, 1.0), LatLon(5.0, 3.0), LatLon(1.0, 3.0), LatLon(3.0, 2.0))
        assertEquals("triangle area", 6181527888.0, areaOf(polyTriangle).toFixed(0), 0.0001)
        assertEquals("square cw area", 12360230987.0, areaOf(polySquareCw).toFixed(0), 0.0001)
        assertEquals("square ccw area", 12360230987.0, areaOf(polySquareCcw).toFixed(0), 0.0001)
        assertEquals("quadrant area", (π * R * R / 2.0).toFixed(1), areaOf(polyQuadrant).toFixed(1), 0.0001)
        assertEquals("hemisphere area", 252198975941606.0, areaOf(polyHemi).toFixed(0), 0.0001) // TODO: spherical gives 252684679676459 (0.2% error) - which is righ, 0.0001t?
        assertEquals("pole area", 16063139192.0, areaOf(polyPole).toFixed(0), 0.0001)
        assertEquals("concave area", 74042699236.0, areaOf(polyConcave).toFixed(0), 0.0001)
        assertEquals("hemisphere enclosed y", true, LatLon(22.5, 0.59).enclosedBy(polyHemi))
        assertEquals("hemisphere enclosed n", false, LatLon(22.5, 0.58).enclosedBy(polyHemi))
        assertEquals("gc enclosed y", true, LatLon(14.0, 45.0).enclosedBy(polyGc))
        assertEquals("gc enclosed n", false, LatLon(15.0, 45.0).enclosedBy(polyGc))
        assertEquals("pole enclosed", true, LatLon(90.0, 0.0).enclosedBy(polyPole))
        assertEquals("polar edge enclosed", true, LatLon(90.0, 0.0).enclosedBy(polyPoleEdge))
        assertEquals("concave enclosed y", true, LatLon(4.0, 2.0).enclosedBy(polyConcave))
        assertEquals("concave enclosed n", false, LatLon(2.0, 2.0).enclosedBy(polyConcave))
    }


    @Test
    fun MeanTest() {
        var points = arrayOf(LatLon(1.0, 1.0), LatLon(2.0, 1.0), LatLon(2.0, 2.0), LatLon(1.0, 2.0))
        assertEquals("mean", "01°30′00″N, 001°30′00″E", meanOf(points).toString())
    }

    @Test
    fun MiscTest() {
        assertEquals("equals true", true, LatLon(52.205, 0.119).equals(LatLon(52.205, 0.119)))
        assertEquals("equals false", false, LatLon(52.206, 0.119).equals(LatLon(52.205, 0.119)))
    }

    @Test
    fun VectorsTest() {
        assertEquals("ll to v", "[0.500,0.500,0.707]", LatLon(45.0, 45.0).toVector().toString())
        assertEquals("v to ll", "45.0000°N, 045.0000°E", Vector3d(0.500, 0.500, 0.707107).toLatLonS().toString("d"))
        assertEquals("great circle", "[-0.794,0.129,0.594]", LatLon(53.3206, -1.7297).greatCircle(96.0).toString())
        assertEquals("gc from vector", "[-0.794,0.129,0.594]", LatLon(53.3206, -1.7297).toVector().greatCircle(96.0).toString())
        assertEquals("divided", "[0.250,0.250,0.354]", Vector3d(0.500, 0.500, 0.707107).div(2.0).toString())
        assertEquals("negate", "[-0.500,-0.500,-0.707]", Vector3d(0.500, 0.500, 0.707107).negate.toString())
        assertEquals("rotate around", "[0.500,-0.146,0.854]", Vector3d(0.500, 0.500, 0.707107).rotateAround(Vector3d(1.0, 0.0, 0.0), π / 4).toString())
    }

}