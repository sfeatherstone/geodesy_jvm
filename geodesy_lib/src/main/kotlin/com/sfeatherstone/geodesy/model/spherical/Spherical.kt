@file:JvmName("Spherical")
package com.sfeatherstone.geodesy.model.spherical

import com.sfeatherstone.geodesy.LatLon
import com.sfeatherstone.geodesy.toDegrees
import com.sfeatherstone.geodesy.toNormalisedDegrees
import com.sfeatherstone.geodesy.toRadians
import kotlin.math.*

/**
 * Library of geodesy functions for operations on a spherical earth model.
 *
 * @module   latlon-spherical
 * @requires dms
 */


/**
 * Returns the distance from ‘this’ point to destination point (using haversine formula).
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number} Distance between this point and destination point, in same units as radius.
 *
 * @example
 *     var p1 = new LatLon(52.205, 0.119);
 *     var p2 = new LatLon(48.857, 2.351);
 *     var d = p1.distanceTo(p2); // 404.3 km
 */
@JvmOverloads
@JvmName("distance")
fun LatLon.distanceTo(point : LatLon, radius : Double = 6371e3):Double {

    // a = sin²(Δφ/2) + cos(φ1)⋅cos(φ2)⋅sin²(Δλ/2)
    // tanδ = √(a) / √(1−a)
    // see mathforum.org/library/drmath/view/51879.html for derivation

    val R = radius
    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()
    val φ2 = point.lat.toRadians()
    val λ2 = point.lon.toRadians()
    val Δφ = φ2 - φ1
    val Δλ = λ2 - λ1

    val a = sin(Δφ/2) * sin(Δφ/2) + cos(φ1) * cos(φ2) * sin(Δλ/2) * sin(Δλ/2)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
    val d = R * c

    return d
}


/**
 * Returns the (initial) bearing from ‘this’ point to destination point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {number} Initial bearing in degrees from north.
 *
 * @example
 *     var p1 = new LatLon(52.205, 0.119);
 *     var p2 = new LatLon(48.857, 2.351);
 *     var b1 = p1.bearingTo(p2); // 156.2°
 */
fun LatLon.bearingTo(point : LatLon): Double {

    // tanθ = sinΔλ⋅cosφ2 / cosφ1⋅sinφ2 − sinφ1⋅cosφ2⋅cosΔλ
    // see mathforum.org/library/drmath/view/55417.html for derivation

    val φ1 = this.lat.toRadians()
    val φ2 = point.lat.toRadians()
    val Δλ = (point.lon-this.lon).toRadians()
    val y = sin(Δλ) * cos(φ2)
    val x = cos(φ1) * sin(φ2) -
            sin(φ1) * cos(φ2) * cos(Δλ)
    val θ = atan2(y, x)

    return (θ.toDegrees()+360) % 360
}


/**
 * Returns final bearing arriving at destination destination point from ‘this’ point; the final bearing
 * will differ from the initial bearing by varying degrees according to distance and latitude.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {number} Final bearing in degrees from north.
 *
 * @example
 *     var p1 = new LatLon(52.205, 0.119);
 *     var p2 = new LatLon(48.857, 2.351);
 *     var b2 = p1.finalBearingTo(p2); // 157.9°
 */
fun LatLon.finalBearingTo(point: LatLon):Double {
    // get initial bearing from destination point to this point & reverse it by adding 180°
    return ( point.bearingTo(this) +180 ) % 360
}


/**
 * Returns the midpoint between ‘this’ point and the supplied point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {LatLon} Midpoint between this point and the supplied point.
 *
 * @example
 *     var p1 = new LatLon(52.205, 0.119);
 *     var p2 = new LatLon(48.857, 2.351);
 *     var pMid = p1.midpointTo(p2); // 50.5363°N, 001.2746°E
 */
fun LatLon.midpointTo(point: LatLon): LatLon {

    // φm = atan2( sinφ1 + sinφ2, √( (cosφ1 + cosφ2⋅cosΔλ) ⋅ (cosφ1 + cosφ2⋅cosΔλ) ) + cos²φ2⋅sin²Δλ )
    // λm = λ1 + atan2(cosφ2⋅sinΔλ, cosφ1 + cosφ2⋅cosΔλ)
    // see mathforum.org/library/drmath/view/51822.html for derivation

    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()
    val φ2 = point.lat.toRadians()
    val Δλ = (point.lon-this.lon).toRadians()

    val Bx = cos(φ2) * cos(Δλ)
    val By = cos(φ2) * sin(Δλ)

    val x = sqrt((cos(φ1) + Bx) * (cos(φ1) + Bx) + By * By)
    val y = sin(φ1) + sin(φ2)
    val φ3 = atan2(y, x)

    val λ3 = λ1 + atan2(By, cos(φ1) + Bx)

    return LatLon(φ3.toDegrees(), λ3.toNormalisedDegrees()) // normalise to −180..+180°
}


/**
 * Returns the point at given fraction between ‘this’ point and specified point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @param   {number} fraction - Fraction between the two points (0 = this point, 1 = specified point).
 * @returns {LatLon} Intermediate point between this point and destination point.
 *
 * @example
 *   let p1 = new LatLon(52.205, 0.119);
 *   let p2 = new LatLon(48.857, 2.351);
 *   let pMid = p1.intermediatePointTo(p2, 0.25); // 51.3721°N, 000.7073°E
 */
fun LatLon.intermediatePointTo(point : LatLon, fraction: Double): LatLon {
    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()
    val φ2 = point.lat.toRadians()
    val λ2 = point.lon.toRadians()
    val sinφ1 = sin(φ1)
    val cosφ1 = cos(φ1)
    val sinλ1 = sin(λ1)
    val cosλ1 = cos(λ1)
    val sinφ2 = sin(φ2)
    val cosφ2 = cos(φ2)
    val sinλ2 = sin(λ2)
    val cosλ2 = cos(λ2)

    // distance between points
    val Δφ = φ2 - φ1
    val Δλ = λ2 - λ1
    val a = sin(Δφ/2.0) * sin(Δφ/2.0) + cos(φ1) * cos(φ2) * sin(Δλ/2.0) * sin(Δλ/2.0)
    val δ = 2.0 * atan2(sqrt(a), sqrt(1.0-a))

    val A = sin((1.0-fraction)*δ) / sin(δ)
    val B = sin(fraction*δ) / sin(δ)

    val x = A * cosφ1 * cosλ1 + B * cosφ2 * cosλ2
    val y = A * cosφ1 * sinλ1 + B * cosφ2 * sinλ2
    val z = A * sinφ1 + B * sinφ2

    val φ3 = atan2(z, sqrt(x*x + y*y))
    val λ3 = atan2(y, x)

    return LatLon(φ3.toDegrees(), λ3.toNormalisedDegrees()) // normalise lon to −180..+180°
}


/**
 * Returns the destination point from ‘this’ point having travelled the given distance on the
 * given initial bearing (bearing normally varies around path followed).
 *
 * @param   {number} distance - Distance travelled, in same units as earth radius (default: metres).
 * @param   {number} bearing - Initial bearing in degrees from north.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {LatLon} Destination point.
 *
 * @example
 *     var p1 = new LatLon(51.4778, -0.0015);
 *     var p2 = p1.destinationPoint(7794, 300.7); // 51.5135°N, 000.0983°W
 */
fun LatLon.destinationPoint(distance: Double, bearing: Double, radius :Double = 6371e3): LatLon {
    // sinφ2 = sinφ1⋅cosδ + cosφ1⋅sinδ⋅cosθ
    // tanΔλ = sinθ⋅sinδ⋅cosφ1 / cosδ−sinφ1⋅sinφ2
    // see mathforum.org/library/drmath/view/52049.html for derivation

    val δ = distance / radius // angular distance in radians
    val θ = bearing.toRadians()

    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()

    val sinφ1 = sin(φ1)
    val cosφ1 = cos(φ1)
    val sinδ = sin(δ)
    val cosδ = cos(δ)
    val sinθ = sin(θ)
    val cosθ = cos(θ)

    val sinφ2 = sinφ1*cosδ + cosφ1*sinδ*cosθ
    val φ2 = asin(sinφ2)
    val y = sinθ * sinδ * cosφ1
    val x = cosδ - sinφ1 * sinφ2
    val λ2 = λ1 + atan2(y, x)

    return LatLon(φ2.toDegrees(), λ2.toNormalisedDegrees()) // normalise to −180..+180°
}


/**
 * Returns the point of intersection of two paths defined by point and bearing.
 *
 * @param   {LatLon} p1 - First point.
 * @param   {number} brng1 - Initial bearing from first point.
 * @param   {LatLon} p2 - Second point.
 * @param   {number} brng2 - Initial bearing from second point.
 * @returns {LatLon|null} Destination point (null if no unique intersection defined).
 *
 * @example
 *     var p1 = LatLon(51.8853, 0.2545), brng1 = 108.547;
 *     var p2 = LatLon(49.0034, 2.5735), brng2 =  32.435;
 *     var pInt = LatLon.intersection(p1, brng1, p2, brng2); // 50.9078°N, 004.5084°E
 */
fun intersection(p1: LatLon, bearing1: Double, p2: LatLon, bearing2: Double): LatLon? {

    // see www.edwilliams.org/avform.htm#Intersection

    val φ1 = p1.lat.toRadians()
    val λ1 = p1.lon.toRadians()
    val φ2 = p2.lat.toRadians()
    val λ2 = p2.lon.toRadians()
    val θ13 = bearing1.toRadians()
    val θ23 = bearing2.toRadians()
    val Δφ = φ2-φ1
    val Δλ = λ2-λ1

    // angular distance p1-p2
    val δ12 = 2.0* asin(sqrt(sin(Δφ/2.0)*sin(Δφ/2.0)
            + cos(φ1)*cos(φ2)*sin(Δλ/2.0)*sin(Δλ/2.0)))

    if (δ12 == 0.0) return null

    // initial/final bearings between points
    var θa = acos(( sin(φ2) - sin(φ1)*cos(δ12) ) / ( sin(δ12)*cos(φ1) ))
    if (java.lang.Double.isNaN(θa)) θa = 0.0 // protect against rounding
    val θb = acos(( sin(φ1) - sin(φ2)*cos(δ12) ) / ( sin(δ12)*cos(φ2) ))

    val θ12 = if (sin(λ2-λ1) >0.0) θa else 2.0*Math.PI-θa
    val θ21 = if (sin(λ2-λ1) >0.0) 2.0*Math.PI-θb else θb

    val α1 = θ13 - θ12 // angle 2-1-3
    val α2 = θ21 - θ23 // angle 1-2-3

    if (sin(α1) ==0.0 && sin(α2) ==0.0) return null // infinite intersections
    if (sin(α1) * sin(α2) < 0.0) return null      // ambiguous intersection

    val α3 = acos(-cos(α1)*cos(α2) + sin(α1)*sin(α2)*cos(δ12))
    val δ13 = atan2(sin(δ12)*sin(α1)*sin(α2), cos(α2)+cos(α1)*cos(α3))
    val φ3 = asin(sin(φ1)*cos(δ13) + cos(φ1)*sin(δ13)*cos(θ13))
    val Δλ13 = atan2(sin(θ13)*sin(δ13)*cos(φ1), cos(δ13)-sin(φ1)*sin(φ3))
    val λ3 = λ1 + Δλ13

    return LatLon(φ3.toDegrees(), λ3.toNormalisedDegrees()) // normalise to −180..+180°
}


/**
 * Returns (signed) distance from ‘this’ point to great circle defined by start-point and end-point.
 *
 * @param   {LatLon} pathStart - Start point of great circle path.
 * @param   {LatLon} pathEnd - End point of great circle path.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number} Distance to great circle (-ve if to left, +ve if to right of path).
 *
 * @example
 *   var pCurrent = new LatLon(53.2611, -0.7972);
 *   var p1 = new LatLon(53.3206, -1.7297);
 *   var p2 = new LatLon(53.1887,  0.1334);
 *   var d = pCurrent.crossTrackDistanceTo(p1, p2);  // -307.5 m
 */
fun LatLon.crossTrackDistanceTo(pathStart: LatLon, pathEnd: LatLon, radius: Double = 6371e3): Double {
    val δ13 = pathStart.distanceTo(this, radius) / radius
    val θ13 = pathStart.bearingTo(this).toRadians()
    val θ12 = pathStart.bearingTo(pathEnd).toRadians()

    val δxt = asin(sin(δ13) * sin(θ13-θ12))

    return δxt * radius
}


/**
 * Returns how far ‘this’ point is along a path from from start-point, heading towards end-point.
 * That is, if a perpendicular is drawn from ‘this’ point to the (great circle) path, the along-track
 * distance is the distance from the start point to where the perpendicular crosses the path.
 *
 * @param   {LatLon} pathStart - Start point of great circle path.
 * @param   {LatLon} pathEnd - End point of great circle path.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number} Distance along great circle to point nearest ‘this’ point.
 *
 * @example
 *   var pCurrent = new LatLon(53.2611, -0.7972);
 *   var p1 = new LatLon(53.3206, -1.7297);
 *   var p2 = new LatLon(53.1887,  0.1334);
 *   var d = pCurrent.alongTrackDistanceTo(p1, p2);  // 62.331 km
 */
fun LatLon.alongTrackDistanceTo(pathStart : LatLon, pathEnd : LatLon, radius :Double = 6371e3): Double {
    val δ13 = pathStart.distanceTo(this, radius) / radius
    val θ13 = pathStart.bearingTo(this).toRadians()
    val θ12 = pathStart.bearingTo(pathEnd).toRadians()

    val δxt = asin(sin(δ13) * sin(θ13-θ12))

    val δat = acos(cos(δ13) / abs(cos(δxt)))

    return δat*(cos(θ12-θ13)).sign * radius
}


/**
 * Returns maximum latitude reached when travelling on a great circle on given bearing from this
 * point ('Clairaut's formula'). Negate the result for the minimum latitude (in the Southern
 * hemisphere).
 *
 * The maximum latitude is independent of longitude; it will be the same for all points on a given
 * latitude.
 *
 * @param {number} bearing - Initial bearing.
 * @param {number} latitude - Starting latitude.
 */
fun LatLon.maxLatitude(bearing: Double): Double {
    val θ = bearing.toRadians()

    val φ = this.lat.toRadians()

    val φMax = acos(abs(sin(θ)*cos(φ)))

    return φMax.toDegrees()
}


/**
 * Returns the pair of meridians at which a great circle defined by two points crosses the given
 * latitude. If the great circle doesn't reach the given latitude, null is returned.
 *
 * @param {LatLon} point1 - First point defining great circle.
 * @param {LatLon} point2 - Second point defining great circle.
 * @param {number} latitude - Latitude crossings are to be determined for.
 * @returns {Object|null} Object containing { lon1, lon2 } or null if given latitude not reached.
 */
fun crossingParallels(point1: LatLon, point2: LatLon, latitude: Double): Pair<Double, Double>? {
    val φ = latitude.toRadians()

    val φ1 = point1.lat.toRadians()
    val λ1 = point1.lon.toRadians()
    val φ2 = point2.lat.toRadians()
    val λ2 = point2.lon.toRadians()

    val Δλ = λ2 - λ1

    val x = sin(φ1) * cos(φ2) * cos(φ) * sin(Δλ)
    val y = sin(φ1) * cos(φ2) * cos(φ) * cos(Δλ) - cos(φ1) * sin(φ2) * cos(φ)
    val z = cos(φ1) * cos(φ2) * sin(φ) * sin(Δλ)

    if (z*z > x*x + y*y) return null // great circle doesn't reach latitude

    val λm = atan2(-y, x)                  // longitude at max latitude
    val Δλi = acos(z / sqrt(x*x+y*y)) // Δλ from λm to intersection points

    val λi1 = λ1 + λm - Δλi
    val λi2 = λ1 + λm + Δλi

    return Pair(λi1.toNormalisedDegrees(), λi2.toNormalisedDegrees()) // normalise to −180..+180°
}


/* Rhumb - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

/**
 * Returns the distance travelling from ‘this’ point to destination point along a rhumb line.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number} Distance in km between this point and destination point (same units as radius).
 *
 * @example
 *     var p1 = new LatLon(51.127, 1.338);
 *     var p2 = new LatLon(50.964, 1.853);
 *     var d = p1.distanceTo(p2); // 40.31 km
 */
fun LatLon.rhumbDistanceTo(point: LatLon, radius: Double = 6371e3): Double {
    // see www.edwilliams.org/avform.htm#Rhumb

    val R = radius
    val φ1 = this.lat.toRadians()
    val φ2 = point.lat.toRadians()
    val Δφ = φ2 - φ1
    var Δλ = abs(point.lon-this.lon).toRadians()
    // if dLon over 180° take shorter rhumb line across the anti-meridian:
    if (Δλ > Math.PI) Δλ -= 2.0*Math.PI

    // on Mercator projection, longitude distances shrink by latitude; q is the 'stretch factor'
    // q becomes ill-conditioned along E-W line (0/0); use empirical tolerance to avoid it
    val Δψ = ln(tan(φ2/2+Math.PI/4)/tan(φ1/2+Math.PI/4))
    val q = if (abs(Δψ) > 10e-12) Δφ/Δψ else cos(φ1)

    // distance is pythagoras on 'stretched' Mercator projection
    val δ = sqrt(Δφ*Δφ + q*q*Δλ*Δλ) // angular distance in radians
    val dist = δ * R

    return dist
}


/**
 * Returns the bearing from ‘this’ point to destination point along a rhumb line.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {number} Bearing in degrees from north.
 *
 * @example
 *     var p1 = new LatLon(51.127, 1.338);
 *     var p2 = new LatLon(50.964, 1.853);
 *     var d = p1.rhumbBearingTo(p2); // 116.7 m
 */
fun LatLon.rhumbBearingTo(point: LatLon): Double {

    val φ1 = this.lat.toRadians()
    val φ2 = point.lat.toRadians()
    var Δλ = (point.lon-this.lon).toRadians()
    // if dLon over 180° take shorter rhumb line across the anti-meridian:
    if (Δλ >  Math.PI) Δλ -= 2*Math.PI
    if (Δλ < -Math.PI) Δλ += 2*Math.PI

    val Δψ = ln(tan(φ2/2+Math.PI/4)/tan(φ1/2+Math.PI/4))

    val θ = atan2(Δλ, Δψ)

    return (θ.toDegrees()+360) % 360
}


/**
 * Returns the destination point having travelled along a rhumb line from ‘this’ point the given
 * distance on the  given bearing.
 *
 * @param   {number} distance - Distance travelled, in same units as earth radius (default: metres).
 * @param   {number} bearing - Bearing in degrees from north.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {LatLon} Destination point.
 *
 * @example
 *     var p1 = new LatLon(51.127, 1.338);
 *     var p2 = p1.rhumbDestinationPoint(40300, 116.7); // 50.9642°N, 001.8530°E
 */
fun LatLon.rhumbDestinationPoint(distance: Double, bearing: Double, radius: Double = 6371e3): LatLon {
    val δ = distance / radius // angular distance in radians
    val φ1 = this.lat.toRadians()
    val λ1 = this.lon.toRadians()
    val θ = bearing.toRadians()

    val Δφ = δ * cos(θ)
    var φ2 = φ1 + Δφ

    // check for some daft bugger going past the pole, normalise latitude if so
    if (abs(φ2) > Math.PI/2){
        φ2 = if (φ2>0) Math.PI-φ2 else -Math.PI-φ2
    }

    val Δψ = ln(tan(φ2/2+Math.PI/4)/tan(φ1/2+Math.PI/4))
    val q = if (abs(Δψ) > 10e-12) Δφ / Δψ else cos(φ1) // E-W course becomes ill-conditioned with 0/0

    val Δλ = δ* sin(θ) /q
    val λ2 = λ1 + Δλ

    return LatLon(φ2.toDegrees(), (λ2.toDegrees() + 540.0) % 360.0 - 180.0) // normalise to −180..+180°
}


/**
 * Returns the loxodromic midpoint (along a rhumb line) between ‘this’ point and second point.
 *
 * @param   {LatLon} point - Latitude/longitude of second point.
 * @returns {LatLon} Midpoint between this point and second point.
 *
 * @example
 *     var p1 = new LatLon(51.127, 1.338);
 *     var p2 = new LatLon(50.964, 1.853);
 *     var pMid = p1.rhumbMidpointTo(p2); // 51.0455°N, 001.5957°E
 */
fun LatLon.rhumbMidpointTo(point: LatLon): LatLon {

    // see mathforum.org/kb/message.jspa?messageID=148837

    val φ1 = this.lat.toRadians()
    var λ1 = this.lon.toRadians()
    val φ2 = point.lat.toRadians()
    val λ2 = point.lon.toRadians()

    if (abs(λ2-λ1) > Math.PI) λ1 += 2*Math.PI // crossing anti-meridian

    val φ3 = (φ1+φ2)/2
    val f1 = tan(Math.PI/4 + φ1/2)
    val f2 = tan(Math.PI/4 + φ2/2)
    val f3 = tan(Math.PI/4 + φ3/2)
    var λ3 = ( (λ2-λ1)* ln(f3) + λ1* ln(f2) - λ2* ln(f1) ) / ln(f2/f1)

    if (!java.lang.Double.isFinite(λ3)) λ3 = (λ1+λ2)/2 // parallel of latitude

    return LatLon(φ3.toDegrees(), (λ3.toDegrees() + 540) % 360 - 180) // normalise to −180..+180°
}


/* Area - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */


/**
 * Calculates the area of a spherical polygon where the sides of the polygon are great circle
 * arcs joining the vertices.
 *
 * @param   {LatLon[]} polygon - Array of points defining vertices of the polygon
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number} The area of the polygon, in the same units as radius.
 *
 * @example
 *   var polygon = [new LatLon(0,0), new LatLon(1,0), new LatLon(0,1)];
 *   var area = areaOf(polygon); // 6.18e9 m²
 */
fun areaOf(polygonInput: Array<LatLon>, radius: Double = 6371e3):Double {
    // uses method due to Karney: osgeo-org.1560.x6.nabble.com/Area-of-a-spherical-polygon-td3841625.html;
    // for each edge of the polygon, tan(E/2) = tan(Δλ/2)·(tan(φ1/2) + tan(φ2/2)) / (1 + tan(φ1/2)·tan(φ2/2))
    // where E is the spherical excess of the trapezium obtained by extending the edge to the equator

    if (polygonInput.size < 3) return 0.0

    // close polygon so that last point equals first point
    val closed = polygonInput[0]==polygonInput[polygonInput.size-1]
    val polygon = if (closed) polygonInput else Array(polygonInput.size+1) { if (it < polygonInput.size) polygonInput[it] else polygonInput[0]}

    if (polygon.size < 4) return 0.0

    val nVertices = polygon.size - 2

    var S = 0.0 // spherical excess in steradians
    for (v in 0..nVertices) {
        val φ1 = polygon[v].lat.toRadians()
        val φ2 = polygon[v+1].lat.toRadians()
        val Δλ = (polygon[v+1].lon - polygon[v].lon).toRadians()
        val E = 2 * atan2(tan(Δλ/2) * (tan(φ1/2)+tan(φ2/2)), 1 + tan(φ1/2)*tan(φ2/2))
        S += E
    }

    // returns whether polygon encloses pole: sum of course deltas around pole is 0° rather than
    // normal ±360°: blog.element84.com/determining-if-a-spherical-polygon-contains-a-pole.html
    fun isPoleEnclosedBy(polygon: Array<LatLon>):Boolean {
        // TODO: any better test than this?
        var ΣΔ = 0.0
        var prevBrng = polygon[0].bearingTo(polygon[1])
        for (v in 0..polygon.size-2) {
            val initBrng = polygon[v].bearingTo(polygon[v + 1])
            val finalBrng = polygon[v].finalBearingTo(polygon[v + 1])
            ΣΔ += (initBrng - prevBrng + 540.0) % 360.0 - 180.0
            ΣΔ += (finalBrng - initBrng + 540.0) % 360.0 - 180.0
            prevBrng = finalBrng
        }
        val initBrng = polygon[0].bearingTo(polygon[1])
        ΣΔ += (initBrng - prevBrng + 540) % 360 - 180
        // TODO: fix (intermittant) edge crossing pole - eg (85,90), (85,0), (85,-90)
        val enclosed = abs(ΣΔ) < 90 // 0°-ish
        return enclosed
    }

    if (isPoleEnclosedBy(polygon)) S = abs(S) - 2*Math.PI

    val A = abs(S * radius*radius) // area in units of R

    return A
}

