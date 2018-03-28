package com.sfeatherstone.geodesy.grids

import com.sfeatherstone.geodesy.*
import kotlin.math.asinh
import kotlin.math.atanh
import kotlin.math.sqrt

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* UTM / WGS-84 Conversion Functions                                  (c) Chris Veness 2014-2017  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong-utm-mgrs.html                                           */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-utm.html                                    */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

/**
 * Convert between Universal Transverse Mercator coordinates and WGS 84 latitude/longitude points.
 *
 * Method based on Karney 2011 ‘Transverse Mercator with an accuracy of a few nanometers’,
 * building on Krüger 1912 ‘Konforme Abbildung des Erdellipsoids in der Ebene’.
 *
 * @module   utm
 * @requires latlon-ellipsoidal
 */
enum class Hemisphere { NORTH, SOUTH }

fun toSingleChar(hemisphere: Hemisphere): Char = when(hemisphere) {
    Hemisphere.NORTH -> 'N'
    Hemisphere.SOUTH -> 'S'
}

fun charToHemisphere(input: Char) : Hemisphere = when(input.toUpperCase()) {
    'N' -> Hemisphere.NORTH
    'S' -> Hemisphere.SOUTH
    else -> throw Exception("Invalid conversion for Hemisphere")
}

/***
 * Utm coordinate object.
 *
 * @property  zone - UTM 6° longitudinal zone (1..60 covering 180°W..180°E).
 * @property  hemisphere - N for northern hemisphere, S for southern hemisphere.
 * @property  easting - Easting in metres from false easting (-500km from central meridian).
 * @property  northing - Northing in metres from equator (N) or from false northing -10,000km (S).
 * @property  convergence - Meridian convergence (bearing of grid north clockwise from true
 *                  north), in degrees
 * @property  scale - Grid scale factor
 */
data class Utm(val zone: Int,
               val hemisphere: Hemisphere,
               val easting: Double,
               val northing: Double,
               val convergence:Double? = null,
               val scale: Double? = null) {
    companion object {
        const val falseEasting = 500e3
        const val falseNorthing = 10000e3
    }

    override fun toString() = toString(0)

    /**
     * Returns a string representation of a UTM coordinate.
     *
     * To distinguish from MGRS grid zone designators, a space is left between the zone and the
     * hemisphere.
     *
     * Note that UTM coordinates get rounded, not truncated (unlike MGRS grid references).
     *
     * @param   digits - Number of digits to appear after the decimal point (3 ≡ mm).
     * @returns A string representation of the coordinate.
     *
     * @example
     *   var utm = parse('31 N 448251 5411932').toString(4);  // 31 N 448251.0000 5411932.0000
     */
    fun toString(digits: Int): String {
        return "%02d %c %s %s".format(zone, toSingleChar(hemisphere), easting.toFixedString(digits), northing.toFixedString(digits))
    }

}


/**
 * Converts latitude/longitude to UTM coordinate.
 *
 * Implements Karney’s method, using Krüger series to order n^6, giving results accurate to 5nm for
 * distances up to 3900km from the central meridian.
 *
 * @returns {Utm}   UTM coordinate.
 * @throws  {Error} If point not valid, if point outside latitude range.
 *
 * @example
 *   var latlong = new LatLon(48.8582, 2.2945);
 *   var utmCoord = toUtm(); // utmCoord.toString(): '31 N 448252 5411933'
 */

fun LatLon.toUtm(): Utm {
    if (this.lat.isNaN() || this.lon.isNaN()) throw Exception("Invalid point")
    if (!(-80.0<=this.lat && this.lat<=84.0)) throw Error("Outside UTM limits")

//    val falseEasting = 500e3
  //  val falseNorthing = 10000e3;

    var zone = Math.floor((this.lon+180.0)/6.0).toInt() + 1 // longitudinal zone
    var λ0 = ((zone-1.0)*6.0 - 180.0 + 3.0).toRadians() // longitude of central meridian

    // ---- handle Norway/Svalbard exceptions
    // grid zones are 8° tall; 0°N is offset 10 into latitude bands array
    val mgrsLatBands = "CDEFGHJKLMNPQRSTUVWXX" // X is repeated for 80-84°N
    val latBand = mgrsLatBands[Math.floor(this.lat/8.0+10.0).toInt()]
    // adjust zone & central meridian for Norway
    if (zone==31 && latBand=='V' && this.lon>= 3) { zone++; λ0 += (6.0).toRadians(); }
    // adjust zone & central meridian for Svalbard
    if (zone==32 && latBand=='X' && this.lon<  9) { zone--; λ0 -= (6.0).toRadians(); }
    if (zone==32 && latBand=='X' && this.lon>= 9) { zone++; λ0 += (6.0).toRadians(); }
    if (zone==34 && latBand=='X' && this.lon< 21) { zone--; λ0 -= (6.0).toRadians(); }
    if (zone==34 && latBand=='X' && this.lon>=21) { zone++; λ0 += (6.0).toRadians(); }
    if (zone==36 && latBand=='X' && this.lon< 33) { zone--; λ0 -= (6.0).toRadians(); }
    if (zone==36 && latBand=='X' && this.lon>=33) { zone++; λ0 += (6.0).toRadians(); }

    val φ = this.lat.toRadians()      // latitude ± from equator
    val λ = this.lon.toRadians() - λ0 // longitude ± from central meridian

    val a = this.datum.ellipsoid.a
    val f = this.datum.ellipsoid.f
    // WGS 84: a = 6378137, b = 6356752.314245, f = 1/298.257223563;

    val k0 = 0.9996 // UTM scale on the central meridian

    // ---- easting, northing: Karney 2011 Eq 7-14, 29, 35:

    val e = Math.sqrt(f*(2.0-f)) // eccentricity
    val n = f / (2.0 - f)        // 3rd flattening
    val n2 = n*n
    val n3 = n*n2
    val n4 = n*n3
    val n5 = n*n4
    val n6 = n*n5 // TODO: compare Horner-form accuracy?

    val cosλ = Math.cos(λ)
    val sinλ = Math.sin(λ)
    val tanλ = Math.tan(λ)

    val τ = Math.tan(φ) // τ ≡ tanφ, τʹ ≡ tanφʹ; prime (ʹ) indicates angles on the conformal sphere
    val σ = Math.sinh(e*atanh(e*τ/ sqrt(1.0+τ*τ)))

    val τʹ = τ*Math.sqrt(1.0+σ*σ) - σ* sqrt(1.0+τ*τ)

    val ξʹ = Math.atan2(τʹ, cosλ)
    val ηʹ = asinh(sinλ / sqrt(τʹ*τʹ + cosλ*cosλ))

    val A = a/(1.0+n) * (1.0 + 1.0/4.0*n2 + 1.0/64.0*n4 + 1.0/256.0*n6) // 2πA is the circumference of a meridian

    val α = arrayOf( 0.0, // note α is one-based array (6th order Krüger expressions)
        1.0/2.0*n - 2.0/3.0*n2 + 5.0/16.0*n3 +   41.0/180.0*n4 -     127.0/288.0*n5 +      7891.0/37800.0*n6,
                  13.0/48.0*n2 -  3.0/5.0*n3 + 557.0/1440.0*n4 +     281.0/630.0*n5 - 1983433.0/1935360.0*n6,
                               61.0/240.0*n3 -  103.0/140.0*n4 + 15061.0/26880.0*n5 +   167603.0/181440.0*n6,
                                           49561.0/161280.0*n4 -     179.0/168.0*n5 + 6601661.0/7257600.0*n6,
                                                                 34729.0/80640.0*n5 - 3418889.0/1995840.0*n6,
                                                                                  212378941.0/319334400.0*n6 )

    var ξ = ξʹ
    for (j in 1..6) ξ += α[j] * Math.sin(2.0*j.toDouble()*ξʹ) * Math.cosh(2.0*j.toDouble()*ηʹ)

    var η = ηʹ
    for (j in 1..6) η += α[j] * Math.cos(2.0*j.toDouble()*ξʹ) * Math.sinh(2.0*j.toDouble()*ηʹ)

    var x = k0 * A * η
    var y = k0 * A * ξ

    // ---- convergence: Karney 2011 Eq 23, 24

    var pʹ = 1.0
    for (j in 1..6) pʹ += 2.0*j.toDouble()*α[j] * Math.cos(2.0*j.toDouble()*ξʹ) * Math.cosh(2.0*j.toDouble()*ηʹ)
    var qʹ = 0.0
    for (j in 1..6) qʹ += 2.0*j.toDouble()*α[j] * Math.sin(2.0*j.toDouble()*ξʹ) * Math.sinh(2.0*j.toDouble()*ηʹ)

    val γʹ = Math.atan(τʹ / Math.sqrt(1.0+τʹ*τʹ)*tanλ)
    val γʺ = Math.atan2(qʹ, pʹ)

    val γ = γʹ + γʺ

    // ---- scale: Karney 2011 Eq 25

    val sinφ = Math.sin(φ)
    val kʹ = Math.sqrt(1.0 - e*e*sinφ*sinφ) * Math.sqrt(1.0 + τ*τ) / Math.sqrt(τʹ*τʹ + cosλ*cosλ)
    val kʺ = A / a * Math.sqrt(pʹ*pʹ + qʹ*qʹ)

    val k = k0 * kʹ * kʺ

    // ------------

    // shift x/y to false origins
    x += Utm.falseEasting             // make x relative to false easting
    if (y < 0.0) y += Utm.falseNorthing // make y in southern hemisphere relative to false northing

    // round to reasonable precision
    x = x.toFixed(6) // nm precision
    y = y.toFixed(6) // nm precision
    val convergence = γ.toDegrees().toFixed(9)
    val scale = k.toFixed(12)

    val h = if (this.lat>=0) Hemisphere.NORTH else Hemisphere.SOUTH // hemisphere

    return Utm(zone, h, x, y, convergence, scale)
}


/**
 * Converts UTM zone/easting/northing coordinate to latitude/longitude
 *
 * @param   utmCoord - UTM coordinate to be converted to latitude/longitude.
 * @returns Latitude/longitude of supplied grid reference.
 *
 * @example
 *   var grid = Utm(31, 'N', 448251.795, 5411932.678);
 *   var latlong = grid.toLatLonE(); // latlong.toString(): 48°51′29.52″N, 002°17′40.20″E
 */

fun Utm.toLatLonE(datum: Datum = WGS84):LatLon {
    val z = this.zone
    val h = this.hemisphere
    var x = this.easting
    var y = this.northing

    if (x.isNaN() || y.isNaN()) throw Exception("Invalid coordinate")

    val a = datum.ellipsoid.a
    val f = datum.ellipsoid.f
    // WGS 84:  a = 6378137, b = 6356752.314245, f = 1/298.257223563;

    val k0 = 0.9996 // UTM scale on the central meridian

    x -= Utm.falseEasting               // make x ± relative to central meridian
    y = if (h== Hemisphere.SOUTH) y - Utm.falseNorthing else y // make y ± relative to equator

    // ---- from Karney 2011 Eq 15-22, 36:

    val e = Math.sqrt(f*(2-f)) // eccentricity
    val n = f / (2 - f)        // 3rd flattening
    val n2 = n*n
    val n3 = n*n2
    val n4 = n*n3
    val n5 = n*n4
    val n6 = n*n5

    val A = a/(1.0+n) * (1.0 + 1.0/4.0*n2 + 1.0/64.0*n4 + 1.0/256.0*n6) // 2πA is the circumference of a meridian

    val η = x / (k0*A)
    val ξ = y / (k0*A)

    val β = arrayOf(0.0, // note β is one-based array (6th order Krüger expressions)
        1.0/2.0*n - 2.0/3.0*n2 + 37.0/96.0*n3 -    1.0/360.0*n4 -   81.0/512.0*n5 +    96199.0/604800.0*n6,
                   1.0/48.0*n2 +  1.0/15.0*n3 - 437.0/1440.0*n4 +   46.0/105.0*n5 - 1118711.0/3870720.0*n6,
                                17.0/480.0*n3 -   37.0/840.0*n4 - 209.0/4480.0*n5 +      5569.0/90720.0*n6,
                                             4397.0/161280.0*n4 -   11.0/504.0*n5 -  830251.0/7257600.0*n6,
                                                               4583.0/161280.0*n5 -  108847.0/3991680.0*n6,
                                                                                 20648693.0/638668800.0*n6 )

    var ξʹ = ξ
    for (j in 1..6) ξʹ -= β[j] * Math.sin(2.0*j.toDouble()*ξ) * Math.cosh(2.0*j.toDouble()*η)

    var ηʹ = η
    for (j in 1..6) ηʹ -= β[j] * Math.cos(2.0*j.toDouble()*ξ) * Math.sinh(2.0*j.toDouble()*η)

    val sinhηʹ = Math.sinh(ηʹ)
    val sinξʹ = Math.sin(ξʹ)
    val cosξʹ = Math.cos(ξʹ)

    val τʹ = sinξʹ / Math.sqrt(sinhηʹ*sinhηʹ + cosξʹ*cosξʹ)

    var τi = τʹ
    do {
        val σi = Math.sinh(e* atanh(e*τi/Math.sqrt(1+τi*τi)))
        val τiʹ = τi * Math.sqrt(1+σi*σi) - σi * Math.sqrt(1+τi*τi)
        val δτi = (τʹ - τiʹ)/Math.sqrt(1.0+τiʹ*τiʹ) * (1.0 + (1.0-e*e)*τi*τi) / ((1.0-e*e)*Math.sqrt(1.0+τi*τi))
        τi += δτi
    } while (Math.abs(δτi) > 1e-12) // using IEEE 754 δτi -> 0 after 2-3 iterations
    // note relatively large convergence test as δτi toggles on ±1.12e-16 for eg 31 N 400000 5000000
    val τ = τi

    val φ = Math.atan(τ)

    var λ = Math.atan2(sinhηʹ, cosξʹ)

    // ---- convergence: Karney 2011 Eq 26, 27

    var p = 1.0
    for (j in 1..6) p -= 2*j*β[j] * Math.cos(2*j*ξ) * Math.cosh(2*j*η)
    var q = 0.0
    for (j in 1..6) q += 2*j*β[j] * Math.sin(2*j*ξ) * Math.sinh(2*j*η)

    val γʹ = Math.atan(Math.tan(ξʹ) * Math.tanh(ηʹ))
    val γʺ = Math.atan2(q, p)

    val γ = γʹ + γʺ

    // ---- scale: Karney 2011 Eq 28

    val sinφ = Math.sin(φ)
    val kʹ = Math.sqrt(1 - e*e*sinφ*sinφ) * Math.sqrt(1 + τ*τ) * Math.sqrt(sinhηʹ*sinhηʹ + cosξʹ*cosξʹ)
    val kʺ = A / a / Math.sqrt(p*p + q*q)

    val k = k0 * kʹ * kʺ

    // ------------

    val λ0 = ((z-1.0)*6.0 - 180.0 + 3.0).toRadians() // longitude of central meridian
    λ += λ0 // move λ from zonal to global coordinates

    // round to reasonable precision
    val lat = φ.toDegrees().toFixed(11) // nm precision (1nm = 10^-11°)
    val lon = λ.toDegrees().toFixed(11) // (strictly lat rounding should be φ⋅cosφ!)
    val convergence = γ.toDegrees().toFixed(9)
    val scale = k.toFixed(12)

    return LatLon(lat, lon, datum, convergence, scale)
}



/**
 * Parses string representation of UTM coordinate.
 *
 * A UTM coordinate comprises (space-separated)
 *  - zone
 *  - hemisphere
 *  - easting
 *  - northing.
 *
 * @param   utmCoord - UTM coordinate (WGS 84).
 * @returns
 * @throws  {Error}  Invalid UTM coordinate.
 *
 * @example
 *   val utmCoord = "31 N 448251 5411932".parseUtm()
 *   // utmCoord: {zone: 31, hemisphere: 'N', easting: 448251, northing: 5411932 }
 */
fun String.parseUtm(): Utm {
    // match separate elements (separated by whitespace)
    val utmCoord = Regex("""\S+""").findAll(this.trim()).toList()

    if (utmCoord.size!=4) throw Exception("Invalid UTM coordinate ‘"+this+"’")

    val zone = utmCoord[0].value.toInt()
    val hemisphere = charToHemisphere(utmCoord[1].value[0])
    val easting = utmCoord[2].value.toDouble()
    val northing = utmCoord[3].value.toDouble()

    return Utm(zone, hemisphere, easting, northing)
}



