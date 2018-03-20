package uk.co.wedgetech.geodesy

import jdk.nashorn.internal.objects.NativeFunction.function
import org.junit.Assert.assertEquals
import org.junit.Test

class OsGridRefTest {
    @Test fun testParse1() {
        assertEquals( OsGridRef(651409.0, 313177.0 ) , "TG 51409 13177".parseOsGridReference())
        assertEquals( OsGridRef(551409.0, 313177.0 ) , "TF 51409 13177".parseOsGridReference())
        assertEquals( OsGridRef(651409.0, 013177.0 ) ,"TW 51409 13177".parseOsGridReference())
        assertEquals( OsGridRef(651400.0, 313170.0 ) ,"TG 5140 1317".parseOsGridReference())
        assertEquals( OsGridRef(600000.0, 300000.0 ) ,"TG 0 0".parseOsGridReference())
        assertEquals( OsGridRef(0.0, 1200000.0 ) ,"HL 0 0".parseOsGridReference())
        assertEquals( OsGridRef(0.0, 0.0 ) ,"sv 0 0".parseOsGridReference())
        assertEquals( OsGridRef(600000.0,1200000.0 ) ,"JM 0 0".parseOsGridReference())
        assertEquals( OsGridRef(699999.0, 399999.0 ) ,"TG 99999 99999".parseOsGridReference())
        assertEquals( OsGridRef(551409.0, 213177.0 ),"TL 51409 13177".parseOsGridReference())
    }

    @Test fun testParse2() {
        val expected = OsGridRef(651409.0, 313177.0 )
        assertEquals( "651409, 313177".parseOsGridReference(), expected )
        assertEquals( "651409 , 313177".parseOsGridReference(), expected )
        assertEquals( "651409,313177".parseOsGridReference(), expected )
        assertEquals( "651409 ,313177".parseOsGridReference(), expected )
    }

    @Test fun testToString(){
        assertEquals( "TG 51409 13177", OsGridRef(651409.0, 313177.0 ).toString(10) )
        assertEquals( "TF 51409 13177", OsGridRef(551409.0, 313177.0 ).toString(10) )
        assertEquals( "TW 51409 13177", OsGridRef(651409.0, 013177.0 ).toString(10) )
        assertEquals( "TG 05140 01317", OsGridRef(605140.0, 301317.0 ).toString(10) )
        assertEquals( "TG 5140 1317", OsGridRef(651400.0, 313170.0 ).toString(8) )
        assertEquals( "TG 00000 00000", OsGridRef(600000.0, 300000.0 ).toString(10) )
        assertEquals( "HL 00000 00000", OsGridRef(0.0, 1200000.0 ).toString(10) )
        assertEquals( "SV 00000 00000", OsGridRef(0.0, 0.0 ).toString(10) )
        assertEquals( "JM 00000 00000", OsGridRef(600000.0,1200000.0 ).toString(10))
        assertEquals( "TG 99999 99999", OsGridRef(699999.0, 399999.0 ).toString(10))
        assertEquals( "TL 51409 13177", OsGridRef(551409.0, 213177.0 ).toString(10))
    }

    @Test fun testToString0Digits(){
        assertEquals( "651409,313177", OsGridRef(651409.0, 313177.0 ).toString(0) )
        assertEquals( "000000,000000", OsGridRef(0.0, 0.0 ).toString(0) )
        assertEquals( "000000.100,000000.100", OsGridRef(0.1, 0.1 ).toString(0) )
        assertEquals( "000000.100,1200000.100", OsGridRef(0.1, 1200000.1 ).toString(0) )
        assertEquals( "000000,1200000", OsGridRef(0.0, 1200000.0 ).toString(0) )
    }

    @Test fun fromJSTest() {
        var osgb = LatLonDatum("52°39′27.2531″N".parseDegreesMinutesSeconds(), "1°43′4.5177″E".parseDegreesMinutesSeconds(), LatLonDatum.OSGB36)
        var gridref = osgb.toOsGrid()
        assertEquals( "C1 E", 651409.903, gridref.easting.toFixed(3), 0.001)
        assertEquals( "C1 N", 313177.270, gridref.northing.toFixed(3), 0.001)

        val osgb2 = gridref.toLatLonDatum(LatLonDatum.OSGB36)
        assertEquals( "C1 round-trip",  "52°39′27.2531″N, 001°43′04.5177″E", osgb2.toString("dms", 4))

        gridref = OsGridRef(651409.903, 313177.270)
        osgb = gridref.toLatLonDatum(LatLonDatum.OSGB36)
        assertEquals( "C2",  "52°39′27.2531″N, 001°43′04.5177″E", osgb2.toString("dms", 4))
        val gridref2 = osgb2.toOsGrid()

        assertEquals("parse 100km origin", "SU 00000 00000", "SU00".parseOsGridReference().toString())
        assertEquals("parse 100km origin", "SU 00000 00000", "SU 0 0".parseOsGridReference().toString())
        assertEquals("parse  no whitespace", "SU 38700 14800", "SU387148".parseOsGridReference().toString())
        assertEquals("parse  6-digit", "SU 38700 14800", "SU 387 148".parseOsGridReference().toString())
        assertEquals("parse  10-digit", "SU 38700 14800", "SU 38700 14800".parseOsGridReference().toString())
        assertEquals("parse  numeric", "SU 38700 14800", "438700,114800".parseOsGridReference().toString())

        val greenwichWGS84 = LatLonDatum(51.4778, -0.0016) // default WGS84
        val greenwichOSGB36 = greenwichWGS84.convertDatum(LatLonDatum.OSGB36)
        assertEquals("convert WGS84 -> OSGB36", "51.4773°N, 000.0000°E" , greenwichOSGB36.toString("d"))
        assertEquals("convert OSGB36 -> WGS84", "51.4778°N, 000.0016°W", greenwichOSGB36.convertDatum(LatLonDatum.WGS84).toString("d"))

        // limits
        assertEquals("SW regular", "SV 00000 00000", OsGridRef( 0.0, 0.0).toString())
        assertEquals("NE regular", "JM 99999 99999", OsGridRef( 699999.0, 1299999.0).toString())
        assertEquals("SW numeric", "000000,000000", OsGridRef( 0.0, 0.0).toString(0))
        assertEquals("NW numeric", "699999,1299999", OsGridRef( 699999.0, 1299999.0).toString(0))

        // DG round-trip

        val dgGridRef: OsGridRef = "TQ 44359 80653".parseOsGridReference()

        // round-tripping OSGB36 works perfectly
        val dgOsgb: LatLonDatum = dgGridRef.toLatLonDatum(LatLonDatum.OSGB36)
        assertEquals("DG round-trip OSGB36",         dgGridRef.toString(), dgOsgb.toOsGrid().toString())
        assertEquals("DG round-trip OSGB36 numeric", "544359,180653", dgOsgb.toOsGrid().toString(0))

        // reversing Helmert transform (OSGB->WGS->OSGB) introduces small error (≈ 3mm in UK), so WGS84
        // round-trip is not quite perfect: test needs to incorporate 3mm error to pass
        val dgWgs = dgGridRef.toLatLonDatum()
        assertEquals("DG round-trip WGS84 numeric",  "544358.997,180653", dgWgs.toOsGrid().toString(0))
    }


}