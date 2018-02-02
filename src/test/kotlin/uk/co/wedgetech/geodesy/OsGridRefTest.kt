package uk.co.wedgetech.geodesy

import org.junit.Assert.assertEquals
import org.junit.Test

class OsGridRefTest {
    @Test fun testParse1() {
        assertEquals( OsGridRef(651409.0, 313177.0 ) ,parse("TG 51409 13177"))
        assertEquals( OsGridRef(551409.0, 313177.0 ) ,parse("TF 51409 13177"))
        assertEquals( OsGridRef(651409.0, 013177.0 ) ,parse("TW 51409 13177"))
        assertEquals( OsGridRef(651400.0, 313170.0 ) ,parse("TG 5140 1317") )
        assertEquals( OsGridRef(600000.0, 300000.0 ) ,parse("TG 0 0"))
        assertEquals( OsGridRef(0.0, 1200000.0 ) ,parse("HL 0 0"))
        assertEquals( OsGridRef(0.0, 0.0 ) ,parse("sv 0 0"))
        assertEquals( OsGridRef(600000.0,1200000.0 ) ,parse("JM 0 0")        )
        assertEquals( OsGridRef(699999.0, 399999.0 ) ,parse("TG 99999 99999"))
        assertEquals( OsGridRef(551409.0, 213177.0 ),parse("TL 51409 13177"))
    }

    @Test fun testParse2() {
        val expected = OsGridRef(651409.0, 313177.0 )
        assertEquals( parse("651409, 313177"), expected )
        assertEquals( parse("651409 , 313177"), expected )
        assertEquals( parse("651409,313177"), expected )
        assertEquals( parse("651409 ,313177"), expected )
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


}