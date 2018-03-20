package uk.co.wedgetech.geodesy

import org.junit.Test

import org.junit.Assert.*

class LatLon_SphericalKtTest {
    val R = 6371e3;
    val π = Math.PI;


    @Test
    fun geodesicsTest() {
        val cambg = LatLon(52.205, 0.119)
        val paris = LatLon(48.857, 2.351);
        assertEquals("distance",          4.043e+5, cambg.distanceTo(paris).toPrecision(4), 1.0)
        assertEquals("distance (miles)",  251.2, cambg.distanceTo(paris, 3959.0).toPrecision(4), 0.0001)
        assertEquals("initial bearing",   156.2, cambg.bearingTo(paris) , 0.1)
        assertEquals("final bearing",     157.9, cambg.finalBearingTo(paris), 0.01)
        assertEquals("midpoint",          "50.5363°N, 001.2746°E", cambg.midpointTo(paris).toString("d"))
        assertEquals("int.point",         "51.3721°N, 000.7073°E", cambg.intermediatePointTo(paris, 0.25).toString("d"))

        val greenwich = LatLon(51.4778, -0.0015)
        val dist = 7794.0
        val brng = 300.7;
        assertEquals("dest’n",            "51.5135°N, 000.0983°W", greenwich.destinationPoint(dist, brng).toString("d"))
        assertEquals("dest’n inc R",      "51.5135°N, 000.0983°W", greenwich.destinationPoint(dist, brng, 6371e3).toString("d"))

        val bradwell = LatLon(53.3206, -1.7297)
        assertEquals("cross-track",       -307.5, LatLon(53.2611, -0.7972).crossTrackDistanceTo(bradwell,  LatLon(53.1887,  0.1334)).toPrecision(4), 0.0001)
        assertEquals("along-track",       6.233e+4, LatLon(53.2611, -0.7972).alongTrackDistanceTo(bradwell,  LatLon(53.1887,  0.1334)).toPrecision(4), 0.0001)

        assertEquals("cross-track NE",    -1.112e+5, LatLon( 1.0,  1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track SE",    1.112e+5, LatLon(-1.0,  1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track SW?",   1.112e+5, LatLon(-1.0, -1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("cross-track NW?",   -1.112e+5, LatLon( 1.0, -1.0).crossTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)

        assertEquals("along-track NE",    1.112e+5, LatLon( 1.0,  1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track SE",    1.112e+5, LatLon(-1.0,  1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track SW",    -1.112e+5, LatLon(-1.0, -1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)
        assertEquals("along-track NW",    -1.112e+5, LatLon( 1.0, -1.0).alongTrackDistanceTo(LatLon(0.0, 0.0), LatLon(0.0, 2.0)).toPrecision(4), 0.0001)

        assertEquals("Clairaut 0°",   90.0,    LatLon(0.0,0.0).maxLatitude( 0.0), 0.0001)
        assertEquals("Clairaut 1°",   89.0,    LatLon(0.0,0.0).maxLatitude( 1.0), 0.0001)
        assertEquals("Clairaut 90°",  0.0,     LatLon(0.0,0.0).maxLatitude(90.0), 0.0001)

        val parallels = crossingParallels(LatLon(0.0,0.0), LatLon(60.0,30.0), 30.0);
        parallels?.let { parallels ->
            assertEquals("parallels 1", "30°00′00″N, 009°35′39″E", LatLon(30.0, parallels.first).toString("dms"))
            assertEquals("parallels 2", "30°00′00″N, 170°24′21″E", LatLon(30.0, parallels.second).toString("dms"))
        }
        assertEquals("parallels -",       null, crossingParallels(LatLon(0.0,0.0), LatLon(10.0,60.0), 60.0))


    }

    @Test
    fun Ed_WilliamsTest() {
        val lax = LatLon("33° 57′N".parseDegreesMinutesSeconds(), "118° 24′W".parseDegreesMinutesSeconds())
        val jfk = LatLon("40° 38′N".parseDegreesMinutesSeconds(), "073° 47′W".parseDegreesMinutesSeconds())
        assertEquals("distance nm",   2144.0, lax.distanceTo(jfk, 180.0*60.0/π).toPrecision(4), 0.001);
        assertEquals("bearing",       66.0, lax.bearingTo(jfk).toPrecision(2), 0.001);
        assertEquals("intermediate",  "34°37′N, 116°33′W", lax.intermediatePointTo(jfk, 100.0/2144.0).toString("dm", 0));
        val d = LatLon("34:30N".parseDegreesMinutesSeconds(), "116:30W".parseDegreesMinutesSeconds());
        assertEquals("cross-track", 7.4523, d.crossTrackDistanceTo(lax, jfk, 180*60/π).toPrecision(5), 0.001);
        assertEquals("intermediate",  "38°40.167′N, 101°37.570′W", lax.intermediatePointTo(jfk, 0.4).toString("dm", 3));
        val reo = LatLon("42.600N".parseDegreesMinutesSeconds(), "117.866W".parseDegreesMinutesSeconds())
        val bke = LatLon("44.840N".parseDegreesMinutesSeconds(), "117.806W".parseDegreesMinutesSeconds());
        assertEquals("intersection",  "43.572°N, 116.189°W", intersection(reo, 51.0, bke, 137.0)?.toString("d", 3));
    }


    @Test
    fun intersections_tests() {
        val N = 0.0
        val E = 90.0
        val S = 180.0
        val W = 270.0
        assertEquals("toward 1,1 N,E nearest", "00.9998°N, 001.0000°E", intersection(LatLon(0.0, 1.0), N, LatLon(1.0, 0.0), E)?.toString("d"))
        assertEquals("toward 1,1 E,N nearest", "00.9998°N, 001.0000°E", intersection(LatLon(1.0, 0.0), E, LatLon(0.0, 1.0), N)?.toString("d"))
        assertEquals("away 1,1 S,W antipodal", "00.9998°S, 179.0000°W", intersection(LatLon(0.0, 1.0), S, LatLon(1.0, 0.0), W)?.toString("d"))
        assertEquals("away 1,1 W,S antipodal", "00.9998°S, 179.0000°W", intersection(LatLon(1.0, 0.0), W, LatLon(0.0, 1.0), S)?.toString("d"))

        assertEquals("1E/90E N,E nearest", "00.0175°N, 179.0000°W", intersection(LatLon(0.0, 1.0), N, LatLon(1.0, 92.0), E)?.toString("d"))

        val stn = LatLon(51.8853, 0.2545)
        val cdg = LatLon(49.0034, 2.5735);
        assertEquals("stn-cdg-bxl", "50.9078°N, 004.5084°E", intersection(stn, 108.547, cdg, 32.435)?.toString("d"))

        assertEquals("coincident points", null, intersection(LatLon(0.0, 1.0), N, LatLon(0.0, 1.0), E))
    }

    @Test
    fun polygonalTest() {
        val polyTriangle  = arrayOf(LatLon(1.0,1.0), LatLon(2.0,1.0), LatLon(1.0,2.0))
        val polySquareCw  = arrayOf(LatLon(1.0,1.0), LatLon(2.0,1.0), LatLon(2.0,2.0), LatLon(1.0,2.0));
        val polySquareCcw = arrayOf(LatLon(1.0,1.0), LatLon(1.0,2.0), LatLon(2.0,2.0), LatLon(2.0,1.0))
        val polyQuadrant  = arrayOf(LatLon(0.0,0.0), LatLon(0.0,90.0), LatLon(90.0,0.0))
        val polyHemi      = arrayOf(LatLon(0.0,1.0), LatLon(45.0,0.0), LatLon(89.0,90.0), LatLon(45.0,180.0), LatLon(0.0,179.0), LatLon(-45.0,180.0), LatLon(-89.0,90.0), LatLon(-45.0,0.0))
        val polyPole      = arrayOf(LatLon(89.0,0.0), LatLon(89.0,120.0), LatLon(89.0,-120.0))
        val polyConcave   = arrayOf(LatLon(1.0,1.0), LatLon(5.0,1.0), LatLon(5.0,3.0), LatLon(1.0,3.0), LatLon(3.0,2.0))
        assertEquals("triangle area",         6181527888.0, areaOf(polyTriangle).toFixed(0), 0.001)
        assertEquals("triangle area radius",  6181527888.0, areaOf(polyTriangle, 6371e3).toFixed(0), 0.001)
        assertEquals("triangle area closed",  6181527888.0, areaOf(polyTriangle + polyTriangle[0]).toFixed(0), 0.001)
        assertEquals("square cw area",        12360230987.0, areaOf(polySquareCw).toFixed(0), 0.001)
        assertEquals("square ccw area",       12360230987.0, areaOf(polySquareCcw).toFixed(0), 0.001)
        assertEquals("quadrant area",         (π*R*R/2).toFixed(1), areaOf(polyQuadrant).toFixed(1), 0.001)
        assertEquals("hemisphere area",       252684679676459.0, areaOf(polyHemi).toFixed(0), 0.001) // TODO: vectors gives 252198975941606 (0.2% error) - which is right?
        assertEquals("pole area",             16063139192.0, areaOf(polyPole).toFixed(0), 0.001)
        assertEquals("concave area",          74042699236.0, areaOf(polyConcave).toFixed(0), 0.001)
    }

    @Test
    fun rhumb_linesTests() { 
        val dov = LatLon(51.127, 1.338)
        val cal = LatLon(50.964, 1.853);
        assertEquals("distance",               4.031e+4, dov.rhumbDistanceTo(cal).toPrecision(4), 0.1)
        assertEquals("distance r",             4.031e+4, dov.rhumbDistanceTo(cal, 6371e3).toPrecision(4), 0.1)
        assertEquals("distance dateline E-W",  LatLon(1.0, -179.0).rhumbDistanceTo(LatLon(1.0, 179.0)).toFixed(6),
                (LatLon(1.0, 1.0).rhumbDistanceTo(LatLon(1.0, -1.0)).toFixed(6)), 0.1)
        assertEquals("bearing",                116.7, dov.rhumbBearingTo(cal).toFixed(1), 0.001)
        assertEquals("bearing dateline",       270.0, LatLon(1.0, -179.0).rhumbBearingTo(LatLon(1.0, 179.0)), 0.001)
        assertEquals("bearing dateline",       90.0, LatLon(1.0, 179.0).rhumbBearingTo(LatLon(1.0, -179.0)), 0.001)
        assertEquals("dest’n",                 "50.9641°N, 001.8531°E", dov.rhumbDestinationPoint(40310.0, 116.7).toString("d"))
        assertEquals("dest’n",                 "50.9641°N, 001.8531°E", dov.rhumbDestinationPoint(40310.0, 116.7, 6371e3).toString("d"))
        assertEquals("dest’n",                 "01.0000°N, 002.0000°E", LatLon(1.0, 1.0).rhumbDestinationPoint(111178.0, 90.0).toString("d"))
        assertEquals("midpoint",               "51.0455°N, 001.5957°E", dov.rhumbMidpointTo(cal).toString("d"))
        assertEquals("midpoint dateline",      "01.0000°N, 179.5000°E", LatLon(1.0, -179.0).rhumbMidpointTo(LatLon(1.0, 178.0)).toString("d"))
    }

    @Test
    fun miscTests() {  
        assertEquals("equals true",   true, LatLon(52.205, 0.119).equals(LatLon(52.205, 0.119)) );
        assertEquals("equals false",  false, LatLon(52.206, 0.119).equals(LatLon(52.205, 0.119)));
    }

}