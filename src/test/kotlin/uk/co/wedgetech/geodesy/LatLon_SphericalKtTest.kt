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
}