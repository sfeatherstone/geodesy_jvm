package uk.co.wedgetech.geodesy

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


}