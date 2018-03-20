import com.sfeatherstone.geodesy.*
import uk.co.wedgetech.geodesy.*

/**
 * Direct and inverse solutions of geodesics on the ellipsoid using Vincenty formulae.
 *
 * From: T Vincenty, "Direct and Inverse Solutions of Geodesics on the Ellipsoid with application of
 *       nested equations", Survey Review, vol XXIII no 176, 1975.
 *       www.ngs.noaa.gov/PUBS_LIB/inverse.pdf.
 *
 * @module  latlon-vincenty
 * @extends latlon-ellipsoidal
 */
/** @class LatLon */


/**
 * Returns the distance between ‘this’ point and destination point along a geodesic, using Vincenty
 * inverse solution.
 *
 * Note: the datum used is of ‘this’ point; distance is on the surface of the ellipsoid (height is
 * ignored).
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns (Number} Distance in metres between points or NaN if failed to converge.
 *
 * @example
 *   var p1 = new LatLon(50.06632, -5.71475);
 *   var p2 = new LatLon(58.64402, -3.07009);
 *   var d = p1.distanceTo(p2); // 969,954.166 m
 */
fun LatLonDatum.distanceTo(point: LatLon) : Double {
    try {
        return this.inverse(point).distance.toFixed(3) // round to 1mm precision
    } catch (e: Exception) {
        return java.lang.Double.NaN // failed to converge
    }
}


/**
 * Returns the initial bearing (forward azimuth) to travel along a geodesic from ‘this’ point to the
 * specified point, using Vincenty inverse solution.
 *
 * Note: the datum used is of ‘this’ point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {number}  initial Bearing in degrees from north (0°..360°) or NaN if failed to converge.
 *
 * @example
 *   var p1 = new LatLon(50.06632, -5.71475);
 *   var p2 = new LatLon(58.64402, -3.07009);
 *   var b1 = p1.initialBearingTo(p2); // 9.1419°
 */
fun LatLonDatum.initialBearingTo(point: LatLon): Double {
    try {
        return this.inverse(point).initialBearing.toFixed(9) // round to 0.00001″ precision
    } catch (e: Exception) {
        return java.lang.Double.NaN // failed to converge
    }
}


/**
 * Returns the final bearing (reverse azimuth) having travelled along a geodesic from ‘this’ point
 * to the specified point, using Vincenty inverse solution.
 *
 * Note: the datum used is of ‘this’ point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {number}  Initial bearing in degrees from north (0°..360°) or NaN if failed to converge.
 *
 * @example
 *   var p1 = new LatLon(50.06632, -5.71475);
 *   var p2 = new LatLon(58.64402, -3.07009);
 *   var b2 = p1.finalBearingTo(p2); // 11.2972°
 */
fun LatLonDatum.finalBearingTo(point: LatLon):Double {
    try {
        return this.inverse(point).finalBearing.toFixed(9) // round to 0.00001″ precision
    } catch (e: Exception) {
        return java.lang.Double.NaN // failed to converge
    }
}


/**
 * Returns the destination point having travelled the given distance along a geodesic given by
 * initial bearing from ‘this’ point, using Vincenty direct solution.
 *
 * Note: the datum used is of ‘this’ point; distance is on the surface of the ellipsoid (height is
 * ignored).
 *
 * @param   {number} distance - Distance travelled along the geodesic in metres.
 * @param   {number} initialBearing - Initial bearing in degrees from north.
 * @returns {LatLon} Destination point.
 *
 * @example
 *   var p1 = new LatLon(-37.95103, 144.42487);
 *   var p2 = p1.destinationPoint(54972.271, 306.86816); // 37.6528°S, 143.9265°E
 */
fun LatLonDatum.destinationPoint(distance :Double, initialBearing: Double): LatLon {
    return direct(distance, initialBearing).point
}


/**
 * Returns the final bearing (reverse azimuth) having travelled along a geodesic given by initial
 * bearing for a given distance from ‘this’ point, using Vincenty direct solution.
 *
 * Note: the datum used is of ‘this’ point; distance is on the surface of the ellipsoid (height is
 * ignored).
 *
 * @param   {number} distance - Distance travelled along the geodesic in metres.
 * @param   {LatLon} initialBearing - Initial bearing in degrees from north.
 * @returns {number} Final bearing in degrees from north (0°..360°).
 *
 * @example
 *   var p1 = new LatLon(-37.95103, 144.42487);
 *   var b2 = p1.finalBearingOn(306.86816, 54972.271); // 307.1736°
 */
fun LatLonDatum.finalBearingOn(distance: Double, initialBearing: Double):Double {
    return this.direct(distance, initialBearing).finalBearing.toFixed(9) // round to 0.00001″ precision
}

internal data class directResult(val point: LatLonDatum, val finalBearing :Double, val iterations: Int)
/**
 * Vincenty direct calculation.
 *
 * @private
 * @param   {number} distance - Distance along bearing in metres.
 * @param   {number} initialBearing - Initial bearing in degrees from north.
 * @returns (Object} Object including point (destination point), finalBearing.
 * @throws  {Error}  If formula failed to converge.
 */
internal fun LatLonDatum.direct(distance: Double, initialBearing: Double):directResult {
    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()
    val α1 = initialBearing.toRadians()
    val s = distance

    val a = this.datum.ellipsoid.a
    val b = this.datum.ellipsoid.b
    val f = this.datum.ellipsoid.f

    val sinα1 = Math.sin(α1)
    val cosα1 = Math.cos(α1)

    val tanU1 = (1-f) * Math.tan(φ1)
    val cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1))
    val sinU1 = tanU1 * cosU1
    val σ1 = Math.atan2(tanU1, cosα1)
    val sinα = cosU1 * sinα1
    val cosSqα = 1 - sinα*sinα
    val uSq = cosSqα * (a*a - b*b) / (b*b)
    val A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)))
    val B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)))

    var cos2σM = 0.0
    var sinσ = 0.0
    var cosσ = 0.0

    var σ = s / (b*A)
    var σʹ = 0.0
    var iterations = 0

    do {
        cos2σM = Math.cos(2*σ1 + σ)
        sinσ = Math.sin(σ)
        cosσ = Math.cos(σ)
        val Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
                B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)))
        σʹ = σ
        σ = s / (b*A) + Δσ
    } while (Math.abs(σ-σʹ) > 1e-12 && ++iterations<100)
    if (iterations >= 100) throw Exception("Formula failed to converge") // not possible!

    val x = sinU1*sinσ - cosU1*cosσ*cosα1
    val φ2 = Math.atan2(sinU1*cosσ + cosU1*sinσ*cosα1, (1-f)*Math.sqrt(sinα*sinα + x*x))
    val λ = Math.atan2(sinσ*sinα1, cosU1*cosσ - sinU1*sinσ*cosα1)
    val C = f/16*cosSqα*(4+f*(4-3*cosSqα))
    val L = λ - (1-C) * f * sinα *
            (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)))
    val λ2 = (λ1+L+3*Math.PI)%(2*Math.PI) - Math.PI  // normalise to -180..+180

    var α2 = Math.atan2(sinα, -x)
    α2 = (α2 + 2*Math.PI) % (2*Math.PI) // normalise to 0..360

    return directResult(point = LatLonDatum(φ2.toDegrees(), λ2.toDegrees(), this.datum),
            finalBearing = α2.toDegrees(),
            iterations = iterations)
}


internal data class inverseResult(val distance: Double, val initialBearing: Double, val finalBearing :Double , val iterations: Int)

/**
 * Vincenty inverse calculation.
 *
 * @private
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {Object} Object including distance, initialBearing, finalBearing.
 * @throws  {Error}  If λ > π or formula failed to converge.
 */

internal fun LatLonDatum.inverse(point: LatLon): inverseResult {
    val p1 = LatLon(this.lat, if (this.lon != -180.0) this.lon else 180.0)
    val p2 = point
    //if (p1.lon == -180) p1.lon = 180;
    val φ1 = p1.lat.toRadians()
    val λ1 = p1.lon.toRadians()
    val φ2 = p2.lat.toRadians()
    val λ2 = p2.lon.toRadians()

    val a = this.datum.ellipsoid.a
    val b = this.datum.ellipsoid.b
    val f = this.datum.ellipsoid.f

    val L = λ2 - λ1
    val tanU1 = (1-f) * Math.tan(φ1)
    val cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1))
    val sinU1 = tanU1 * cosU1
    val tanU2 = (1-f) * Math.tan(φ2)
    val cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2))
    val sinU2 = tanU2 * cosU2

    var sinλ = 0.0
    var cosλ = 0.0
    //var sinSqσ : Double
    var sinσ=0.0
    var cosσ=0.0
    var σ=0.0
    //var sinα
    var cosSqα=0.0
    var cos2σM=0.0
    //var C;

    var λ = L
    //var λʹ =
    var iterations = 0
    do {
        sinλ = Math.sin(λ)
        cosλ = Math.cos(λ)
        val sinSqσ = (cosU2*sinλ) * (cosU2*sinλ) + (cosU1*sinU2-sinU1*cosU2*cosλ) * (cosU1*sinU2-sinU1*cosU2*cosλ)
        if (sinSqσ == 0.0) break // co-incident points
        sinσ = Math.sqrt(sinSqσ)
        cosσ = sinU1*sinU2 + cosU1*cosU2*cosλ
        σ = Math.atan2(sinσ, cosσ)
        val sinα = cosU1 * cosU2 * sinλ / sinσ
        cosSqα = 1 - sinα*sinα
        cos2σM = if (cosSqα != 0.0) (cosσ - 2*sinU1*sinU2/cosSqα) else 0.0  // equatorial line: cosSqα=0 (§6)
        val C = f/16*cosSqα*(4+f*(4-3*cosSqα))
        val λʹ = λ
        λ = L + (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)))
        if (Math.abs(λ) > Math.PI) throw Exception("λ > π")
    } while (Math.abs(λ-λʹ) > 1e-12 && ++iterations<1000)
    if (iterations >= 1000) throw Exception("Formula failed to converge")

    val uSq = cosSqα * (a*a - b*b) / (b*b)
    val A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)))
    val B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)))
    val Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
            B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)))

    val s = b*A*(σ-Δσ)

    var α1 = Math.atan2(cosU2*sinλ,  cosU1*sinU2-sinU1*cosU2*cosλ)
    var α2 = Math.atan2(cosU1*sinλ, -sinU1*cosU2+cosU1*sinU2*cosλ)

    α1 = (α1 + 2*Math.PI) % (2*Math.PI) // normalise to 0..360
    α2 = (α2 + 2*Math.PI) % (2*Math.PI) // normalise to 0..360

    return inverseResult(distance = s,
            initialBearing= if (s==0.0) java.lang.Double.NaN else α1.toDegrees(),
            finalBearing = if (s==0.0) java.lang.Double.NaN else α2.toDegrees(),
            iterations = iterations)

}
