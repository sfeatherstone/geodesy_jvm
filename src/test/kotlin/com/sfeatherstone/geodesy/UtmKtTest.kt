package com.sfeatherstone.geodesy

import org.junit.Test

import org.junit.Assert.*
import parseMgsr
import parseUtm
import toLatLonE
import toMgrs
import toUtm

class UtmKtTest {

    @Test
    fun UtmMtmTests() {
        // http://geographiclib.sourceforge.net/cgi-bin/GeoConvert
        // http://www.rcn.montana.edu/resources/converter.aspx

        // latitude/longitude -> UTM
        assertEquals("LL->UTM 0,0",       "31 N 166021.443081 0.000000",      LatLon( 0.0,  0.0).toUtm().toString(6))
        assertEquals("LL->UTM 1,1",       "31 N 277438.263521 110597.972524", LatLon( 1.0,  1.0).toUtm().toString(6))
        assertEquals("LL->UTM -1,-1",     "30 S 722561.736479 9889402.027476",LatLon(-1.0, -1.0).toUtm().toString(6))
        assertEquals("LL->UTM eiffel tower",       "31 N 448251.898 5411943.794", LatLon( 48.8583,   2.2945).toUtm().toString(3))
        assertEquals("LL->UTM sidney o/h",        "56 S 334873.199 6252266.092", LatLon(-33.857,  151.215 ).toUtm().toString(3))
        assertEquals("LL->UTM white house",       "18 N 323394.296 4307395.634", LatLon( 38.8977, -77.0365).toUtm().toString(3))
        assertEquals("LL->UTM rio christ",        "23 S 683466.254 7460687.433", LatLon(-22.9519, -43.2106).toUtm().toString(3))
        assertEquals("LL->UTM bergen",            "32 N 297508.410 6700645.296", LatLon( 60.39135,  5.3249).toUtm().toString(3))
        assertEquals("LL->UTM bergen convergence", -3.196281440, LatLon( 60.39135,  5.3249).toUtm().convergence?:0.0, 0.000000001 )
        assertEquals("LL->UTM bergen scale",       1.000102473211, LatLon( 60.39135,  5.3249).toUtm().scale?:0.0, 0.000000000001)

        // UTM -> latitude/longitude
        assertEquals("UTM->LL 0,0",                LatLon(0.0, 0.0).toString(), "31 N 166021.443081 0.000000".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL 1,1",                LatLon( 1.0,  1.0).toString(),"31 N 277438.263521 110597.972524".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL -1,-1",              LatLon(-1.0, -1.0).toString(),"30 S 722561.736479 9889402.027476".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL eiffel tower",       LatLon( 48.8583,   2.2945).toString(),"31 N 448251.898 5411943.794".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL sidney o/h",         LatLon(-33.857,  151.215 ).toString(),"56 S 334873.199 6252266.092".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL white house",        LatLon( 38.8977, -77.0365).toString(),"18 N 323394.296 4307395.634".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL rio christ",         LatLon(-22.9519, -43.2106).toString(),"23 S 683466.254 7460687.433".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL bergen",             LatLon( 60.39135,  5.3249).toString(),"32 N 297508.410 6700645.296".parseUtm().toLatLonE().toString())
        assertEquals("UTM->LL bergen convergence", -3.196281443, "32 N 297508.410 6700645.296".parseUtm().toLatLonE().convergence?:0.0, 0.000000001)
        assertEquals("UTM->LL bergen scale",  1.000102473212,     "32 N 297508.410 6700645.296".parseUtm().toLatLonE().scale?:0.0, 0.000000000001)

        // UTM -> MGRS
        assertEquals("UTM->MGRS 0,0",              "31N AA 66021 00000", "31 N 166021.443081 0.000000".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS 1,1",              "31N BB 77438 10597", "31 N 277438.263521 110597.972524".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS -1,-1",            "30M YD 22561 89402", "30 S 722561.736479 9889402.027476".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS eiffel tower",     "31U DQ 48251 11943", "31 N 448251.898 5411943.794".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS sidney o/h",       "56H LH 34873 52266", "56 S 334873.199 6252266.092".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS white house",      "18S UJ 23394 07395", "18 N 323394.296 4307395.634".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS rio christ",       "23K PQ 83466 60687", "23 S 683466.254 7460687.433".parseUtm().toMgrs().toString())
        assertEquals("UTM->MGRS bergen",           "32V KN 97508 00645", "32 N 297508.410 6700645.296".parseUtm().toMgrs().toString())

        // MGRS -> UTM
        assertEquals("MGRS->UTM 0,0",              "31 N 166021 0", "31N AA 66021 00000".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM 1,1",              "31 N 277438 110597","31N BB 77438 10597".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM -1,-1",           "30 S 722561 9889402", "30M YD 22561 89402".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM eiffel tower",    "31 N 448251 5411943", "31U DQ 48251 11943".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM sidney o/h",      "56 S 334873 6252266", "56H LH 34873 52266".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM white house",     "18 N 323394 4307395", "18S UJ 23394 07395".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM rio christ",      "23 S 683466 7460687", "23K PQ 83466 60687".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM bergen",          "32 N 297508 6700645", "32V KN 97508 00645".parseMgsr()?.toUtm().toString())
        // forgiving parsing of 100km squares spanning bands
        assertEquals("MGRS->UTM 01P ≡ UTM 01Q",   "01 N 500000 1768935", "01P ET 00000 68935".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS->UTM 01Q ≡ UTM 01P",   "01 N 500000 1768935", "01Q ET 00000 68935".parseMgsr()?.toUtm().toString())
        // military style
        assertEquals("MGRS->UTM 0,0 military",     "31 N 166021 0", "31NAA6602100000".parseMgsr()?.toUtm().toString())

        // https://www.ibm.com/developerworks/library/j-coordconvert/#listing7 (note UTM/MGRS confusion; UTM is rounded, MGRS is truncated; UPS not included)
        assertEquals("IBM #01 UTM->LL",            "00.0000°N, 000.0000°W","31 N 166021 0".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #02 UTM->LL",            "00.1300°N, 000.2324°W", "30 N 808084 14385".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #03 UTM->LL",            "45.6456°S, 023.3545°E", "34 S 683473 4942631".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #04 UTM->LL",            "12.7650°S, 033.8765°W", "25 S 404859 8588690".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #09 UTM->LL",            "23.4578°N, 135.4545°W", "08 N 453580 2594272".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #10 UTM->LL",            "77.3450°N, 156.9876°E", "57 N 450793 8586116".parseUtm().toLatLonE().toString("d"))
        assertEquals("IBM #01 LL->UTM",            "31 N 166021 0", LatLon(  0.0000,    0.0000).toUtm().toString())
        assertEquals("IBM #01 LL->MGRS",           "31N AA 66021 00000", LatLon(  0.0000,    0.0000).toUtm().toMgrs().toString())
        assertEquals("IBM #02 LL->UTM",            "30 N 808084 14386", LatLon(  0.1300,   -0.2324).toUtm().toString(0))
        assertEquals("IBM #02 LL->MGRS",           "30N ZF 08084 14385", LatLon(  0.1300,   -0.2324).toUtm().toMgrs().toString())
        assertEquals("IBM #03 LL->UTM",            "34 S 683474 4942631", LatLon(-45.6456,   23.3545).toUtm().toString(0))
        assertEquals("IBM #03 LL->MGRS",           "34G FQ 83473 42631", LatLon(-45.6456,   23.3545).toUtm().toMgrs().toString())
        assertEquals("IBM #04 LL->UTM",            "25 S 404859 8588691", LatLon(-12.7650,  -33.8765).toUtm().toString(0))
        assertEquals("IBM #04 LL->MGRS",           "25L DF 04859 88691", LatLon(-12.7650,  -33.8765).toUtm().toMgrs().toString())
        assertEquals("IBM #09 LL->UTM",            "08 N 453580 2594273", LatLon( 23.4578, -135.4545).toUtm().toString(0))
        assertEquals("IBM #09 LL->MGRS",           "08Q ML 53580 94272", LatLon( 23.4578, -135.4545).toUtm().toMgrs().toString())
        assertEquals("IBM #10 LL->UTM",            "57 N 450794 8586116", LatLon( 77.3450,  156.9876).toUtm().toString(0))
        assertEquals("IBM #10 LL->MGRS",           "57X VF 50793 86116", LatLon( 77.3450,  156.9876).toUtm().toMgrs().toString())

        // varying resolution
        assertEquals("MGRS 4-digit -> UTM",        "12 N 252000 3786000", "12S TC 52 86".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS 10-digit -> UTM",       "12 N 252000 3786000", "12S TC 52000 86000".parseMgsr()?.toUtm().toString())
        assertEquals("MGRS 10-digit+decimals",     "12 N 252000.123 3786000.123", "12S TC 52000.123 86000.123".parseMgsr()?.toUtm()?.toString(3))
        assertEquals("MGRS truncate",              "12S TC 529 869", "12S TC 52999.999 86999.999".parseMgsr()?.toString(6))
        assertEquals("MGRS-UTM round",             "12 N 253000 3787000", "12S TC 52999.999 86999.999".parseMgsr()?.toUtm().toString())

    }
}