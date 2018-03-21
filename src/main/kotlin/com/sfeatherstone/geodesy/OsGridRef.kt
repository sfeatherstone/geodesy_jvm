package com.sfeatherstone.geodesy

import com.sfeatherstone.geodesy.ellipsoidal.convertDatum

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Ordnance Survey Grid Reference functions                           (c) Chris Veness 2005-2017  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong-gridref.html                                            */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-osgridref.html                              */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


/**
 * Convert OS grid references to/from OSGB latitude/longitude points.
 *
 * Formulation implemented here due to Thomas, Redfearn, etc is as published by OS, but is inferior
 * to Krüger as used by e.g. Karney 2011.
 *
 * www.ordnancesurvey.co.uk/docs/support/guide-coordinate-systems-great-britain.pdf.
 *
 * @module   osgridref
 * @requires latlon-ellipsoidal
 */
/*
 * Converted 2015 to work with WGS84 by default, OSGB36 as option;
 * www.ordnancesurvey.co.uk/blog/2014/12/confirmation-on-changes-to-latitude-and-longitude
 */


/**
 * Creates an OsGridRef object.
 *
 * @constructor
 * @param {number} easting - Easting in metres from OS false origin.
 * @param {number} northing - Northing in metres from OS false origin.
 *
 * @example
 *   var grid = new OsGridRef(651409, 313177);
 */
data class OsGridRef(val easting : Double, val northing: Double) {

    override fun toString() :String = toString(10)

    /**
     * Converts ‘this’ numeric grid reference to standard OS grid reference.
     *
     * @param   {number} [digits=10] - Precision of returned grid reference (10 digits = metres);
     *   digits=0 will return grid reference in numeric format.
     * @returns {string} This grid reference in standard format.
     *
     * @example
     *   var ref = new OsGridRef(651409, 313177).toString(); // TG 51409 13177
     */
    fun toString(digits: Int): String
    {
        if (digits % 2 != 0 || digits > 16) throw IllegalArgumentException("Invalid precision $digits")

        var e = this.easting
        var n = this.northing
        if ( e == Double.NaN || n == Double.NaN) throw IllegalArgumentException("Invalid grid reference")

        // use digits = 0 to return numeric format (in metres, allowing for decimals & for northing > 1e6)
        if (digits == 0) {
            //Format "000000.010,1000000.010" or "000000,000000" if whole numbers
            return (if (Math.floor(e)==e) "%06d".format(Math.floor(e).toInt()) else "%010.3f".format(e)) +
                    "," +
                    (if (Math.floor(n)==n) "%06d".format(Math.floor(n).toInt()) else "%010.3f".format(n))
        }

        // get the 100km-grid indices
        val e100k = Math.floor(e / 100000)
        val n100k = Math.floor(n / 100000)

        if (e100k < 0 || e100k > 6 || n100k < 0 || n100k > 12) return ""

        // translate those into numeric equivalents of the grid letters
        var l1 = (19 - n100k) - (19 - n100k) % 5 + Math.floor((e100k + 10) / 5)
        var l2 = (19 - n100k) * 5 % 25 + e100k % 5

        // compensate for skipped 'I' and build grid letter-pairs
        if (l1 > 7) l1++
        if (l2 > 7) l2++
        //val letterPair = String.fromCharCode(l1 + 'A'.charCodeAt(0), l2 + 'A'.charCodeAt(0));
        val letterPair = "${(l1 + 'A'.toInt()).toChar()}${(l2 + 'A'.toInt()).toChar()}"

        // strip 100km-grid indices from easting & northing, and reduce precision
        val halfDigits : Int = digits / 2
        e = Math.floor((e % 100000) / Math.pow(10.0, 5.0 - halfDigits))
        n = Math.floor((n % 100000) / Math.pow(10.0, 5.0 - halfDigits))

        // pad eastings & northings with leading zeros (just in case, allow up to 16-digit (mm) refs)
        val formatString = "%0$halfDigits.0f"
        val eStr = formatString.format(e)
        val nStr = formatString.format(n)
        val sb = StringBuilder()
                .append(letterPair)
                .append(" ")
                .append(eStr)
                .append(" ")
                .append(nStr)

        return sb.toString()
    }
}

/**
 * Parses grid reference to OsGridRef object.
 *
 * Accepts standard grid references (eg 'SU 387 148'), with or without whitespace separators, from
 * two-digit references up to 10-digit references (1m × 1m square), or fully numeric comma-separated
 * references in metres (eg '438700,114800').
 *
 * @param   {string}    gridref - Standard format OS grid reference.
 * @returns {OsGridRef} Numeric version of grid reference in metres from false origin (SW corner of
 *   supplied grid square).
 * @throws Error on Invalid grid reference.
 *
 * @example
 *   var grid = OsGridRef.parse('TG 51409 13177'); // grid: { easting: 651409, northing: 313177 }
 */
fun String.parseOsGridReference() : OsGridRef
{
    val gridref = this.trim().toUpperCase()

    // check for fully numeric comma-separated gridref format
    //val gridRefRegEx = Regex(gridref)
    val gridRefRegEx = Regex("^(\\d+)\\s*,\\s*(\\d+)$")
    val match = gridRefRegEx.matchEntire(gridref)
    match?.let {
        return OsGridRef(it.groupValues[1].toDouble(), it.groupValues[2].toDouble())
    }

    // validate format
    val valid = Regex("^[A-Z]{2}\\s*[0-9]+\\s*[0-9]+$")

//        match = gridref.matches("^[A-Z]{2}\\s*[0-9]+\\s*[0-9]+$/i");
    if (!valid.matches(gridref)) throw IllegalArgumentException("Invalid grid reference")

    // get numeric values of letter references, mapping A->0, B->1, C->2, etc:
    val gridRefUpper = gridref.toUpperCase()
    val AasInt = 'A'.toInt()
    var l1: Int = gridRefUpper.get(0).toInt() - AasInt
    var l2: Int = gridRefUpper.get(1).toInt() - AasInt
    // shuffle down letters after 'I' since 'I' is not used in grid:
    if (l1 > 7) l1--
    if (l2 > 7) l2--

    // convert grid letters into 100km-square indexes from false origin (grid square SV):
    val e100km :Int = ((l1 - 2) % 5) * 5 + (l2 % 5)
    val n100km :Int = (19 - Math.floor(l1 / 5.0).toInt() * 5) - Math.floor(l2 / 5.0).toInt()

    // skip grid letters to get numeric (easting/northing) part of ref
    var en = gridref.substring(2).trim().split(Regex("\\s+"))
    // if e/n not whitespace separated, split half way
    if (en.size == 1) {
        en = listOf(en[0].substring(IntRange(0, (en[0].length / 2)-1)), en[0].substring(en[0].length / 2))
    }

    // validation
    if (e100km < 0 || e100km > 6 || n100km < 0 || n100km > 12) throw IllegalArgumentException ("Invalid grid reference")
    if (en.size != 2) throw IllegalArgumentException("Invalid grid reference")
    if (en[0].length != en[1].length) throw IllegalArgumentException ("Invalid grid reference")

    // standardise to 10-digit refs (metres)
    val easString = (en[0] + "00000").substring(IntRange(0, 4))
    val norString = (en[1] + "00000").substring(IntRange(0, 4))

    val e = e100km.toString() + easString
    val n = n100km.toString() + norString

    return OsGridRef(e.toDouble(), n.toDouble())
}

/**
 * Converts latitude/longitude to Ordnance Survey grid reference easting/northing coordinate.
 *
 * Note formulation implemented here due to Thomas, Redfearn, etc is as published by OS, but is
 * inferior to Krüger as used by e.g. Karney 2011.
 *
 * @param   {LatLon}    point - latitude/longitude.
 * @returns {OsGridRef} OS Grid Reference easting/northing.
 *
 * @example
 *   var p = new LatLon(52.65798, 1.71605);
 *   var grid = OsGridRef.latLonToOsGrid(p); // grid.toString(): TG 51409 13177
 *   // for conversion of (historical) OSGB36 latitude/longitude point:
 *   var p = new LatLon(52.65757, 1.71791, LatLon.datum.OSGB36);
 */
fun LatLonDatum.toOsGrid(): OsGridRef {
    // if necessary convert to OSGB36 first
    val osgbPoint = if (this.datum != LatLonDatum.OSGB36) this.convertDatum(LatLonDatum.OSGB36)
        else this

    val φ = osgbPoint.lat.toRadians()
    val λ = osgbPoint.lon.toRadians()

    val a = 6377563.396
    val b = 6356256.909              // Airy 1830 major & minor semi-axes
    val F0 = 0.9996012717             // NatGrid scale factor on central meridian
    val φ0 = (49.0).toRadians()
    val λ0 = (-2.0).toRadians()      // NatGrid true origin is 49°N,2°W
    val N0 = -100000.0
    val E0 = 400000.0                  // northing & easting of true origin, metres
    val e2 = 1 - (b * b) / (a * a)   // eccentricity squared
    val n = (a - b) / (a + b)
    val n2 = n * n
    val n3 = n * n * n         // n, n², n³

    val cosφ = Math.cos(φ)
    val sinφ = Math.sin(φ)
    val ν = a * F0 / Math.sqrt(1 - e2 * sinφ * sinφ)            // nu = transverse radius of curvature
    val ρ = a * F0 * (1.0 - e2) / Math.pow(1.0 - e2 * sinφ * sinφ, 1.5) // rho = meridional radius of curvature
    val η2 = ν / ρ - 1.0                                    // eta = ?

    val Ma = (1.0 + n + (5.0 / 4.0) * n2 + (5.0 / 4.0) * n3) * (φ - φ0)
    val Mb = (3.0 * n + 3.0 * n * n + (21.0 / 8.0) * n3) * Math.sin(φ - φ0) * Math.cos(φ + φ0)
    val Mc = ((15.0 / 8.0) * n2 + (15.0 / 8.0) * n3) * Math.sin(2.0 * (φ - φ0)) * Math.cos(2 * (φ + φ0))
    val Md = (35.0 / 24.0) * n3 * Math.sin(3.0 * (φ - φ0)) * Math.cos(3.0 * (φ + φ0))
    val M = b * F0 * (Ma - Mb + Mc - Md)              // meridional arc

    val cos3φ = cosφ * cosφ * cosφ
    val cos5φ = cos3φ * cosφ * cosφ
    val tan2φ = Math.tan(φ) * Math.tan(φ)
    val tan4φ = tan2φ * tan2φ

    val I = M + N0
    val II = (ν / 2.0) * sinφ * cosφ
    val III = (ν / 24.0) * sinφ * cos3φ * (5.0 - tan2φ + 9.0 * η2)
    val IIIA = (ν / 720.0) * sinφ * cos5φ * (61.0 - 58.0 * tan2φ + tan4φ)
    val IV = ν * cosφ
    val V = (ν / 6.0) * cos3φ * (ν / ρ - tan2φ)
    val VI = (ν / 120.0) * cos5φ * (5.0 - 18.0 * tan2φ + tan4φ + 14.0 * η2 - 58.0 * tan2φ * η2)

    val Δλ = λ - λ0
    val Δλ2 = Δλ * Δλ
    val Δλ3 = Δλ2 * Δλ
    val Δλ4 = Δλ3 * Δλ
    val Δλ5 = Δλ4 * Δλ
    val Δλ6 = Δλ5 * Δλ

    val N = I + II * Δλ2 + III * Δλ4 + IIIA * Δλ6
    val E = E0 + IV * Δλ + V * Δλ3 + VI * Δλ5

    return OsGridRef(E.toFixed(3), N.toFixed(3)) // gets truncated to SW corner of 1m grid square
}


/**
 * Converts Ordnance Survey grid reference easting/northing coordinate to latitude/longitude
 * (SW corner of grid square).
 *
 * Note formulation implemented here due to Thomas, Redfearn, etc is as published by OS, but is
 * inferior to Krüger as used by e.g. Karney 2011.
 *
 * @param   {OsGridRef}    gridref - Grid ref E/N to be converted to lat/long (SW corner of grid square).
 * @param   {LatLon.datum} [datum=WGS84] - Datum to convert grid reference into.
 * @returns {LatLon}       Latitude/longitude of supplied grid reference.
 *
 * @example
 *   var gridref = new OsGridRef(651409.903, 313177.270);
 *   var pWgs84 = OsGridRef.osGridToLatLon(gridref);                     // 52°39′28.723″N, 001°42′57.787″E
 *   // to obtain (historical) OSGB36 latitude/longitude point:
 *   var pOsgb = OsGridRef.osGridToLatLon(gridref, LatLon.datum.OSGB36); // 52°39′27.253″N, 001°43′04.518″E
 */
fun OsGridRef.toLatLonDatum(datum: LatLonDatum.Datum = LatLonDatum.WGS84): LatLonDatum
{
    val E = this.easting
    val N = this.northing

    val a = 6377563.396
    val b = 6356256.909              // Airy 1830 major & minor semi-axes
    val F0 = 0.9996012717                             // NatGrid scale factor on central meridian
    val φ0 = (49.0).toRadians()
    val λ0 = (-2.0).toRadians()  // NatGrid true origin is 49°N,2°W
    val N0 = -100000.0
    val E0 = 400000.0                     // northing & easting of true origin, metres
    val e2 = 1.0 - (b * b) / (a * a)                          // eccentricity squared
    val n = (a - b) / (a + b)
    val n2 = n*n
    val n3 = n*n*n         // n, n², n³

    var φ = φ0
    var M = 0.0
    do {
        φ += (N - N0 - M) / (a * F0)

        val Ma = (1.0 + n + (5.0 / 4.0) * n2 + (5.0 / 4.0) * n3) * (φ - φ0)
        val Mb = (3.0 * n + 3.0 * n * n + (21.0 / 8.0) * n3) * Math.sin(φ - φ0) * Math.cos(φ + φ0)
        val Mc = ((15.0 / 8.0) * n2 + (15.0 / 8.0) * n3) * Math.sin(2.0 * (φ - φ0)) * Math.cos(2.0 * (φ + φ0))
        val Md = (35.0 / 24.0) * n3 * Math.sin(3.0 * (φ - φ0)) * Math.cos(3.0 * (φ + φ0))
        M = b * F0 * (Ma - Mb + Mc - Md)              // meridional arc

    } while ((N - N0 - M) >= 0.00001)  // ie until < 0.01mm

    val cosφ = Math.cos(φ)
    val sinφ = Math.sin(φ)
    val ν = a * F0 / Math.sqrt(1.0 - (e2 * sinφ * sinφ))            // nu = transverse radius of curvature
    val ρ = a * F0 * (1.0 - e2) / Math.pow(1.0 - (e2 * sinφ * sinφ), 1.5) // rho = meridional radius of curvature
    val η2 = ν / ρ - 1.0                                    // eta = ?

    val tanφ = Math.tan(φ)
    val tan2φ = tanφ * tanφ
    val tan4φ = tan2φ*tan2φ
    val tan6φ = tan4φ*tan2φ
    val secφ = 1.0 / cosφ
    val ν3 = ν * ν * ν
    val ν5 = ν3*ν*ν
    val ν7 = ν5*ν*ν
    val VII = tanφ / (2.0 * ρ * ν)
    val VIII = tanφ / (24.0 * ρ * ν3) * (5.0 + 3.0 * tan2φ + η2 - 9.0 * tan2φ * η2)
    val IX = tanφ / (720.0 * ρ * ν5) * (61.0 + 90.0 * tan2φ + 45.0 * tan4φ)
    val X = secφ / ν
    val XI = secφ / (6.0 * ν3) * (ν / ρ + 2.0 * tan2φ)
    val XII = secφ / (120.0 * ν5) * (5.0 + 28.0 * tan2φ + 24.0 * tan4φ)
    val XIIA = secφ / (5040.0 * ν7) * (61.0 + 662.0 * tan2φ + 1320.0 * tan4φ + 720.0 * tan6φ)

    val dE = (E - E0)
    val dE2 = dE*dE
    val dE3 = dE2*dE
    val dE4 = dE2*dE2
    val dE5 = dE3*dE2
    val dE6 = dE4*dE2
    val dE7 = dE5*dE2
    φ = φ - VII * dE2 + VIII * dE4 - IX * dE6
    val λ = λ0 + X * dE - XI * dE3 + XII * dE5 - XIIA * dE7

    var point = LatLonDatum(φ.toDegrees(), λ.toDegrees(), LatLonDatum.OSGB36)
    if (datum != LatLonDatum.OSGB36) point = point.convertDatum(datum)

    return point
}
