package com.sfeatherstone.geodesy.model.ellipsoidal

import com.sfeatherstone.geodesy.LatLon
import com.sfeatherstone.geodesy.toFixed
import org.junit.Test

import org.junit.Assert.*

class VincentyKtTest {

    @Test
    fun latlonVincentyTests() {
        var le = LatLon(50.06632, -5.71475)
        val jog = LatLon(58.64402, -3.07009)
        assertEquals("inverse distance",             969954.166, le.distanceTo(jog).toFixed(3), 0.00001)
        assertEquals("inverse initial bearing",      9.1419, le.initialBearingTo(jog).toFixed(4), 0.00001)
        assertEquals("inverse final bearing",        11.2972, le.finalBearingTo(jog).toFixed(4), 0.00001)

        val flindersPeak = LatLon(-37.95103, 144.42487)
        val buninyong = LatLon(-37.6528, 143.9265)
        assertEquals("direct destination",                 buninyong.toString("d"), flindersPeak.destinationPoint(54972.271, 306.86816).toString("d"))
        assertEquals("direct final brng",                   307.1736 , flindersPeak.finalBearingOn(54972.271, 306.86816).toFixed(4), 0.00001)
        assertEquals("antipodal distance",                    19936288.579, LatLon(0.0, 0.0).distanceTo(LatLon(0.5, 179.5)), 0.00001)

        assertEquals("antipodal convergence failure dist",    Double.NaN, LatLon(0.0, 0.0).distanceTo(LatLon(0.5, 179.7)), 0.1)
        assertEquals("antipodal convergence failure brng i",  Double.NaN, LatLon(0.0, 0.0).initialBearingTo(LatLon(0.5, 179.7)), 0.1)
        assertEquals("antipodal convergence failure brng f",  Double.NaN, LatLon(0.0, 0.0).finalBearingTo(LatLon(0.5, 179.7)), 0.1)

        assertEquals("inverse coincident distance",           0.0, le.distanceTo(le), 0.00001)
        assertEquals("inverse coincident initial bearing",    Double.NaN, le.initialBearingTo(le),0.1)
        assertEquals("inverse coincident final bearing",      Double.NaN, le.finalBearingTo(le),0.1)
        assertEquals("inverse equatorial distance",           111319.491, LatLon(0.0,0.0).distanceTo(LatLon(0.0,1.0)), 0.00001)
        assertEquals("direct coincident destination",         le.toString("d"), le.destinationPoint(0.0, 0.0).toString("d"))

        assertEquals("Q1 a",  4015703.021, LatLon( 30.0, 30.0).distanceTo(LatLon( 60.0, 60.0)), 0.000001)
        assertEquals("Q1 b",  4015703.021, LatLon( 60.0, 60.0).distanceTo(LatLon( 30.0, 30.0)), 0.000001)
        assertEquals("Q1 c",  4015703.021, LatLon( 30.0, 60.0).distanceTo(LatLon( 60.0, 30.0)), 0.000001)
        assertEquals("Q1 d",  4015703.021, LatLon( 60.0, 30.0).distanceTo(LatLon( 30.0, 60.0)), 0.000001)
        assertEquals("Q2 a",  4015703.021, LatLon( 30.0,-30.0).distanceTo(LatLon( 60.0,-60.0)), 0.000001)
        assertEquals("Q2 b",  4015703.021, LatLon( 60.0,-60.0).distanceTo(LatLon( 30.0,-30.0)), 0.000001)
        assertEquals("Q2 c",  4015703.021, LatLon( 30.0,-60.0).distanceTo(LatLon( 60.0,-30.0)), 0.000001)
        assertEquals("Q2 d",  4015703.021, LatLon( 60.0,-30.0).distanceTo(LatLon( 30.0,-60.0)), 0.000001)
        assertEquals("Q3 a",  4015703.021, LatLon(-30.0,-30.0).distanceTo(LatLon(-60.0,-60.0)), 0.000001)
        assertEquals("Q3 b",  4015703.021, LatLon(-60.0,-60.0).distanceTo(LatLon(-30.0,-30.0)), 0.000001)
        assertEquals("Q3 c",  4015703.021, LatLon(-30.0,-60.0).distanceTo(LatLon(-60.0,-30.0)), 0.000001)
        assertEquals("Q3 d",  4015703.021, LatLon(-60.0,-30.0).distanceTo(LatLon(-30.0,-60.0)), 0.000001)
        assertEquals("Q4 a",  4015703.021, LatLon(-30.0, 30.0).distanceTo(LatLon(-60.0, 60.0)), 0.000001)
        assertEquals("Q4 b",  4015703.021, LatLon(-60.0, 60.0).distanceTo(LatLon(-30.0, 30.0)), 0.000001)
        assertEquals("Q4 c",  4015703.021, LatLon(-30.0, 60.0).distanceTo(LatLon(-60.0, 30.0)), 0.000001)
        assertEquals("Q4 d",  4015703.021, LatLon(-60.0, 30.0).distanceTo(LatLon(-30.0, 60.0)), 0.000001)
    }
}