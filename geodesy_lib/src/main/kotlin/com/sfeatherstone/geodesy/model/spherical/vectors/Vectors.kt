@file:JvmName("Vectors")
package com.sfeatherstone.geodesy.model.spherical.vectors

import com.sfeatherstone.geodesy.LatLon
import com.sfeatherstone.geodesy.toDegrees
import com.sfeatherstone.geodesy.toRadians
import com.sfeatherstone.geodesy.Vector3d
import java.util.*
import kotlin.math.*

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/*  Vector-based spherical geodetic (latitude/longitude) functions    (c) Chris Veness 2011-2017  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong-vectors.html                                            */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-latlon-nvector-spherical.html               */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


/**
 * Tools for working with points and paths on (a spherical model of) the earth’s surface using a
 * vector-based approach using ‘n-vectors’ (rather than the more common spherical trigonometry;
 * a vector-based approach makes many calculations much simpler, and easier to follow, compared
 * with trigonometric equivalents).
 *
 * Note on a spherical model earth, an n-vector is equivalent to a normalised version of an (ECEF)
 * cartesian coordinate.
 *
 * @module   latlon-vectors
 * @requires vector3d
 * @requires dms
 */


/**
 * Converts ‘this’ lat/lon point to Vector3d n-vector (normal to earth's surface).
 *
 * @returns {Vector3d} Normalised n-vector representing lat/lon point.
 *
 * @example
 *   var p = new LatLon(45, 45);
 *   var v = p.com.sfeatherstone.geodesy.model.spherical.vectors.toVector(); // [0.5000,0.5000,0.7071]
 */
fun LatLon.toVector(): Vector3d {
    val φ = lat.toRadians()
    val λ = lon.toRadians()

    // right-handed vector: x -> 0°E,0°N; y -> 90°E,0°N, z -> 90°N
    val x = cos(φ) * cos(λ)
    val y = cos(φ) * sin(λ)
    val z = sin(φ)

    return Vector3d(x, y, z)
}


/**
 * Converts ‘this’ (geocentric) cartesian vector to (spherical) latitude/longitude point.
 *
 * @returns  {LatLon} Latitude/longitude point vector points to.
 *
 * @example
 *   var v = new Vector3d(0.500, 0.500, 0.707);
 *   var p = v.com.sfeatherstone.geodesy.model.spherical.vectors.toLatLonS(); // 45.0°N, 45.0°E
 */
fun Vector3d.toLatLonS(): LatLon {
    val φ = atan2(this.z, sqrt(this.x*this.x + this.y*this.y))
    val λ = atan2(this.y, this.x)

    return LatLon(φ.toDegrees(), λ.toDegrees())
}


/**
 * N-vector normal to great circle obtained by heading on given bearing from ‘this’ point.
 *
 * Direction of vector is such that initial bearing vector b = c × p.
 *
 * @param   {number}   bearing - Compass bearing in degrees.
 * @returns {Vector3d} Normalised vector representing great circle.
 *
 * @example
 *   var p1 = new LatLon(53.3206, -1.7297);
 *   var gc = p1.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(96.0); // [-0.794,0.129,0.594]
 */
fun LatLon.greatCircle(bearing: Double): Vector3d {
    val φ = this.lat.toRadians()
    val λ = this.lon.toRadians()
    val θ = bearing.toRadians()

    val x =  sin(λ) * cos(θ) - sin(φ) * cos(λ) * sin(θ)
    val y = -cos(λ) * cos(θ) - sin(φ) * sin(λ) * sin(θ)
    val z =  cos(φ) * sin(θ)

    return Vector3d(x, y, z)
}


/**
 * N-vector normal to great circle obtained by heading on given bearing from point given by ‘this’
 * n-vector.
 *
 * Direction of vector is such that initial bearing vector b = c × p.
 *
 * @param   {number}   bearing - Compass bearing in degrees.
 * @returns {Vector3d} Normalised vector representing great circle.
 *
 * @example
 *   var n1 = new LatLon(53.3206, -1.7297).toNvector();
 *   var gc = n1.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(96.0); // [-0.794,0.129,0.594]
 */
fun Vector3d.greatCircle(bearing : Double): Vector3d {
    val θ = bearing.toRadians()

    val N = Vector3d(0.0, 0.0, 1.0)
    val e = N.cross(this) // easting
    val n = this.cross(e) // northing
    val eʹ = e.times(cos(θ) /e.length)
    val nʹ = n.times(sin(θ) /n.length)
    val c = nʹ.minus(eʹ)

    return c
}


/**
 * Returns the distance from ‘this’ point to the specified point.
 *
 * @param   point - Latitude/longitude of destination point.
 * @param   radius=6371e3 - (Mean) radius of earth (defaults to radius in metres).
 * @returns Distance between this point and destination point, in same units as radius.
 *
 * @example
 *   var p1 = new LatLon(52.205, 0.119);
 *   var p2 = new LatLon(48.857, 2.351);
 *   var d = p1.com.sfeatherstone.geodesy.vectors.com.sfeatherstone.geodesy.model.ellipsoidal.distanceTo(p2); // 404.3 km
 */
@JvmOverloads
@JvmName("distance")
fun LatLon.distanceTo(point: LatLon, radius: Double = 6371e3):Double {
    val p1 = this.toVector()
    val p2 = point.toVector()

    val δ = p1.angleTo(p2) // δ = atan2(|p₁×p₂|, p₁·p₂)
    val d = δ * radius

    return d
}


/**
 * Returns the (initial) bearing from ‘this’ point to the specified point, in compass degrees.
 *
 * @param   {LatLon}    point - Latitude/longitude of destination point.
 * @returns {number}    Initial bearing in degrees from North (0°..360°).
 * @throws  {TypeError} Point is not LatLon object.
 *
 * @example
 *   var p1 = new LatLon(52.205, 0.119);
 *   var p2 = new LatLon(48.857, 2.351);
 *   var b1 = p1.com.sfeatherstone.geodesy.model.spherical.vectors.bearingTo(p2); // 156.2°
 */
fun LatLon.bearingTo(point: LatLon): Double {
    val p1 = this.toVector()
    val p2 = point.toVector()

    val N = Vector3d(0.0, 0.0, 1.0) // n-vector representing north pole

    val c1 = p1.cross(p2) // great circle through p1 & p2
    val c2 = p1.cross(N)  // great circle through p1 & north pole

    val θ = c1.angleTo(c2, p1) // bearing is (signed) angle between c1 & c2

    return (θ.toDegrees()+360) % 360 // normalise to 0..360
}


/**
 * Returns the midpoint between ‘this’ point and specified point.
 *
 * @param   {LatLon} point - Latitude/longitude of destination point.
 * @returns {LatLon} Midpoint between this point and destination point.
 *
 * @example
 *   var p1 = new LatLon(52.205, 0.119);
 *   var p2 = new LatLon(48.857, 2.351);
 *   var pMid = p1.com.sfeatherstone.geodesy.model.spherical.vectors.midpointTo(p2); // 50.5363°N, 001.2746°E
 */
fun LatLon.midpointTo(point: LatLon): LatLon {
    val p1 = this.toVector()
    val p2 = point.toVector()

    val mid = p1.plus(p2).unit()

    return mid.toLatLonS()
}


/**
 * Returns the point at given fraction between ‘this’ point and specified point.
 *
 * @param   {LatLon}    point - Latitude/longitude of destination point.
 * @param   {number}    fraction - Fraction between the two points (0 = this point, 1 = specified point).
 * @returns {LatLon}    Intermediate point between this point and destination point.
 * @throws  {TypeError} Point is not LatLon object.
 *
 * @example
 *   var p1 = new LatLon(52.205, 0.119);
 *   var p2 = new LatLon(48.857, 2.351);
 *   var pInt = p1.com.sfeatherstone.geodesy.model.spherical.vectors.intermediatePointTo(p2, 0.25); // 51.3721°N, 000.7073°E
 */
fun LatLon.intermediatePointTo(point: LatLon, fraction: Double): LatLon {
    // angular distance between points; tanδ = |n₁×n₂| / n₁⋅n₂
    val n1 = this.toVector()
    val n2 = point.toVector()
    val sinθ = n1.cross(n2).length
    val cosθ = n1.dot(n2)
    val δ = atan2(sinθ, cosθ)

    // interpolated angular distance on straight line between points
    val δi = δ * fraction
    val sinδi = sin(δi)
    val cosδi = cos(δi)

    // direction vector (perpendicular to n1 in plane of n2)
    val d = n1.cross(n2).unit().cross(n1) // unit(n₁×n₂) × n₁

    // interpolated position
    val int = n1.times(cosδi).plus(d.times(sinδi)) // n₁⋅cosδᵢ + d⋅sinδᵢ

    return Vector3d(int.x, int.y, int.z).toLatLonS()
}


/**
 * Returns the latitude/longitude point projected from the point at given fraction on a straight
 * line between between ‘this’ point and specified point.
 *
 * @param   {LatLon}    point - Latitude/longitude of destination point.
 * @param   {number}    fraction - Fraction between the two points (0 = this point, 1 = specified point).
 * @returns {LatLon}    Intermediate point between this point and destination point.
 * @throws  {TypeError} Point is not LatLon object.
 *
 * @example
 *   var p1 = new LatLon(52.205, 0.119);
 *   var p2 = new LatLon(48.857, 2.351);
 *   var pInt = p1.com.sfeatherstone.geodesy.model.spherical.vectors.intermediatePointOnChordTo(p2, 0.25); // 51.3723°N, 000.7072°E
 */
fun LatLon.intermediatePointOnChordTo(point: LatLon, fraction: Double): LatLon {
    val n1 = this.toVector()
    val n2 = point.toVector()

    val int = n1.plus(n2.minus(n1).times(fraction)) // n₁ + (n₂−n₁)·f ≡ n₁·(1-f) + n₂·f

    return Vector3d(int.x, int.y, int.z).toLatLonS()
}


/**
 * Returns the destination point from ‘this’ point having travelled the given distance on the
 * given initial bearing (bearing will normally vary before destination is reached).
 *
 * @param   {number} distance - Distance travelled, in same units as earth radius (default: metres).
 * @param   {number} bearing - Initial bearing in degrees from north.
 * @param   {number} [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {LatLon} Destination point.
 *
 * @example
 *   var p1 = new LatLon(51.4778, -0.0015);
 *   var p2 = p1.com.sfeatherstone.geodesy.vectors.com.sfeatherstone.geodesy.model.ellipsoidal.destinationPoint(7794, 300.7); // 51.5135°N, 000.0983°W
 */
fun LatLon.destinationPoint(distance: Double, bearing: Double, radius: Double = 6371e3): LatLon {
    val n1 = this.toVector()
    val δ = distance / radius // angular distance in radians
    val θ = bearing.toRadians()

    val N = Vector3d(0.0, 0.0, 1.0) // north pole

    val de = N.cross(n1).unit()   // east direction vector @ n1
    val dn = n1.cross(de)         // north direction vector @ n1

    val deSinθ = de.times(sin(θ))
    val dnCosθ = dn.times(cos(θ))

    val d = dnCosθ.plus(deSinθ)   // direction vector @ n1 (≡ C×n1; C = great circle)

    val x = n1.times(cos(δ)) // component of n2 parallel to n1
    val y = d.times(sin(δ))  // component of n2 perpendicular to n1

    val n2 = x.plus(y)

    return n2.toLatLonS()
}


/**
 * Returns the point of intersection of two paths each defined by point pairs or start point and bearing.
 *
 * @param   {LatLon}        path1start - Start point of first path.
 * @param   {LatLon|number} path1brngEnd - End point of first path or initial bearing from first start point.
 * @param   {LatLon}        path2start - Start point of second path.
 * @param   {LatLon|number} path2brngEnd - End point of second path or initial bearing from second start point.
 * @returns {LatLon}        Destination point (null if no unique intersection defined)
 *
 * @example
 *   var p1 = LatLon(51.8853, 0.2545), brng1 = 108.55;
 *   var p2 = LatLon(49.0034, 2.5735), brng2 =  32.44;
 *   var pInt = LatLon.intersection(p1, brng1, p2, brng2); // 50.9076°N, 004.5086°E
 */

/*
fun LatLon.intersection(path1start: LatLon, path1brngEnd: LatLon, path2start: LatLon, path2brngEnd: LatLon) {
    if (!(path1start instanceof LatLon)) throw new TypeError('path1start is not LatLon object');
    if (!(path2start instanceof LatLon)) throw new TypeError('path2start is not LatLon object');
    if (!(path1brngEnd instanceof LatLon) && isNaN(path1brngEnd)) throw new TypeError('path1brngEnd is not LatLon object or bearing');
    if (!(path2brngEnd instanceof LatLon) && isNaN(path2brngEnd)) throw new TypeError('path2brngEnd is not LatLon object or bearing');

    // if c1 & c2 are great circles through start and end points (or defined by start point + bearing),
    // then candidate intersections are simply c1 × c2 & c2 × c1; most of the work is deciding correct
    // intersection point to select! if bearing is given, that determines which intersection, if both
    // paths are defined by start/end points, take closer intersection

    var p1 = path1start.com.sfeatherstone.geodesy.model.spherical.vectors.toVector();
    var p2 = path2start.com.sfeatherstone.geodesy.model.spherical.vectors.toVector();

    var c1, c2, path1def, path2def;
    // c1 & c2 are vectors defining great circles through start & end points; p × c gives initial bearing vector

    if (path1brngEnd instanceof LatLon) { // path 1 defined by endpoint
        c1 = p1.cross(path1brngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector());
        path1def = 'endpoint';
    } else {                              // path 1 defined by initial bearing
        c1 = path1start.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(Number(path1brngEnd));
        path1def = 'bearing';
    }
    if (path2brngEnd instanceof LatLon) { // path 2 defined by endpoint
        c2 = p2.cross(path2brngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector());
        path2def = 'endpoint';
    } else {                              // path 2 defined by initial bearing
        c2 = path2start.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(Number(path2brngEnd));
        path2def = 'bearing';
    }

    // there are two (antipodal) candidate intersection points; we have to choose which to return
    var i1 = c1.cross(c2);
    var i2 = c2.cross(c1);

    // am I making heavy weather of this? is there a simpler way to do it?

    // selection of intersection point depends on how paths are defined (bearings or endpoints)
    var intersection=null, dir1=null, dir2=null;
    switch (path1def+'+'+path2def) {
        case 'bearing+bearing':
            // if c×p⋅i1 is +ve, the initial bearing is towards i1, otherwise towards antipodal i2
            dir1 = Math.sign(c1.cross(p1).dot(i1)); // c1×p1⋅i1 +ve means p1 bearing points to i1
            dir2 = Math.sign(c2.cross(p2).dot(i1)); // c2×p2⋅i1 +ve means p2 bearing points to i1

            switch (dir1+dir2) {
                case  2: // dir1, dir2 both +ve, 1 & 2 both pointing to i1
                    intersection = i1;
                    break;
                case -2: // dir1, dir2 both -ve, 1 & 2 both pointing to i2
                    intersection = i2;
                    break;
                case  0: // dir1, dir2 opposite; intersection is at further-away intersection point
                    // take opposite intersection from mid-point of p1 & p2 [is this always true?]
                    intersection = p1.plus(p2).dot(i1) > 0 ? i2 : i1;
                    break;
            }
            break;
        case 'bearing+endpoint': // use bearing c1 × p1
            dir1 = Math.sign(c1.cross(p1).dot(i1)); // c1×p1⋅i1 +ve means p1 bearing points to i1
            intersection = dir1>0 ? i1 : i2;
            break;
        case 'endpoint+bearing': // use bearing c2 × p2
            dir2 = Math.sign(c2.cross(p2).dot(i1)); // c2×p2⋅i1 +ve means p2 bearing points to i1
            intersection = dir2>0 ? i1 : i2;
            break;
        case 'endpoint+endpoint': // select nearest intersection to mid-point of all points
            var mid = p1.plus(p2).plus(path1brngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector()).plus(path2brngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector());
            intersection = mid.dot(i1)>0 ? i1 : i2;
            break;
    }

    return intersection.com.sfeatherstone.geodesy.model.spherical.vectors.toLatLonS();
};
*/


/**
 * Returns (signed) distance from ‘this’ point to great circle defined by start-point and end-point/bearing.
 *
 * @param   {LatLon}        pathStart - Start point of great circle path.
 * @param   {LatLon|number} pathBrngEnd - End point of great circle path or initial bearing from great circle start point.
 * @param   {number}        [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number}        Distance to great circle (-ve if to left, +ve if to right of path).
 *
 * @example
 *   var pCurrent = new LatLon(53.2611, -0.7972);
 *
 *   var p1 = new LatLon(53.3206, -1.7297), brng = 96.0;
 *   var d = pCurrent.crossTrackDistanceTo(p1, brng);// -305.7 m
 *
 *   var p1 = new LatLon(53.3206, -1.7297), p2 = new LatLon(53.1887, 0.1334);
 *   var d = pCurrent.crossTrackDistanceTo(p1, p2);  // -307.5 m
 */
/*
fun LatLon.crossTrackDistanceTo(pathStart: LatLon, pathBrngEnd, radius: Double = 6371e3): Double {

    var p = this.com.sfeatherstone.geodesy.model.spherical.vectors.toVector();

    var gc = pathBrngEnd instanceof LatLon                   // (note JavaScript is not good at method overloading)
        ? pathStart.com.sfeatherstone.geodesy.model.spherical.vectors.toVector().cross(pathBrngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector()) // great circle defined by two points
        : pathStart.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(Number(pathBrngEnd));        // great circle defined by point + bearing

    var α = gc.angleTo(p) - Math.PI/2; // angle between point & great-circle

    var d = α * radius;

    return d;
};
*/

/**
 * Returns how far ‘this’ point is along a path from from start-point, heading on bearing or towards
 * end-point. That is, if a perpendicular is drawn from ‘this’ point to the (great circle) path, the
 * along-track distance is the distance from the start point to where the perpendicular crosses the
 * path.
 *
 * @param   {LatLon}        pathStart - Start point of great circle path.
 * @param   {LatLon|number} pathBrngEnd - End point of great circle path or initial bearing from great circle start point.
 * @param   {number}        [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number}        Distance along great circle to point nearest ‘this’ point.
 *
 * @example
 *   var pCurrent = new LatLon(53.2611, -0.7972);
 *   var p1 = new LatLon(53.3206, -1.7297);
 *   var p2 = new LatLon(53.1887,  0.1334);
 *   var d = pCurrent.alongTrackDistanceTo(p1, p2);  // 62.331 km
 */
/*
fun LatLon.alongTrackDistanceTo(pathStart, pathBrngEnd, radius: Double = 6371e3) {
    if (!(pathStart instanceof LatLon)) throw new TypeError('pathStart is not LatLon object');

    var p = this.com.sfeatherstone.geodesy.model.spherical.vectors.toVector();

    var gc = pathBrngEnd instanceof LatLon                   // (note JavaScript is not good at method overloading)
        ? pathStart.com.sfeatherstone.geodesy.model.spherical.vectors.toVector().cross(pathBrngEnd.com.sfeatherstone.geodesy.model.spherical.vectors.toVector()) // great circle defined by two points
        : pathStart.com.sfeatherstone.geodesy.model.spherical.vectors.greatCircle(Number(pathBrngEnd));        // great circle defined by point + bearing

    var pat = gc.cross(p).cross(gc); // along-track point c × p × c

    var α = pathStart.com.sfeatherstone.geodesy.model.spherical.vectors.toVector().angleTo(pat, gc); // angle between start point and along-track point

    var d = α * radius;

    return d;
};
*/

/**
 * Returns closest point on great circle segment between point1 & point2 to ‘this’ point.
 *
 * If this point is ‘within’ the extent of the segment, the point is on the segment between point1 &
 * point2; otherwise, it is the closer of the endpoints defining the segment.
 *
 * @param   {LatLon} point1 - Start point of great circle segment.
 * @param   {LatLon} point2 - End point of great circle segment.
 * @returns {number} point on segment.
 *
 * @example
 *   var p1 = new LatLon(51.0, 1.0), p2 = new LatLon(51.0, 2.0);
 *
 *   var p0 = new LatLon(51.0, 1.9);
 *   var p = p0.com.sfeatherstone.geodesy.model.spherical.vectors.nearestPointOnSegment(p1, p2); // 51.0004°N, 001.9000°E
 *   var d = p.com.sfeatherstone.geodesy.vectors.com.sfeatherstone.geodesy.model.ellipsoidal.distanceTo(p);                  // 42.71 m
 *
 *   var p0 = new LatLon(51.0, 2.1);
 *   var p = p0.com.sfeatherstone.geodesy.model.spherical.vectors.nearestPointOnSegment(p1, p2); // 51.0000°N, 002.0000°E
 */
fun LatLon.nearestPointOnSegment(point1: LatLon, point2: LatLon): LatLon {
    if (this.isBetween(point1, point2)) {
        // closer to segment than to its endpoints, find closest point on segment
        val n0 = this.toVector()
        val n1 = point1.toVector()
        val n2 = point2.toVector()
        val c1 = n1.cross(n2) // n1×n2 = vector representing great circle through p1, p2
        val c2 = n0.cross(c1) // n0×c1 = vector representing great circle through p0 normal to c1
        val n = c1.cross(c2)  // c2×c1 = nearest point on c1 to n0
        return n.toLatLonS()
    }

    // beyond segment extent, take closer endpoint
    val d1 = this.distanceTo(point1)
    val d2 = this.distanceTo(point2)
    return if (d1<d2) point1 else point2
}


/**
 * Returns whether this point is between point 1 & point 2.
 *
 * If this point is not on the great circle defined by point1 & point 2, returns whether this point
 * is within area bound by perpendiculars to the great circle at each point (in the same hemisphere).
 *
 * @param   {LatLon}  point1 - First point defining segment.
 * @param   {LatLon}  point2 - Second point defining segment.
 * @returns {boolean} Whether this point is within extent of segment.
 */
fun LatLon.isBetween(point1: LatLon, point2: LatLon):Boolean {
    val n0 = this.toVector()
    val n1 = point1.toVector()
    val n2 = point2.toVector() // n-vectors

    // get vectors representing p0->p1, p0->p2, p1->p2, p2->p1
    val δ10 = n0.minus(n1)
    val δ12 = n2.minus(n1)
    val δ20 = n0.minus(n2)
    val δ21 = n1.minus(n2)

    // dot product δ10⋅δ12 tells us if p0 is on p2 side of p1, similarly for δ20⋅δ21
    val extent1 = δ10.dot(δ12)
    val extent2 = δ20.dot(δ21)

    val isBetween = extent1>=0 && extent2>=0
    val isSameHemisphere = n0.dot(n1)>=0 && n0.dot(n2)>=0

    return isBetween && isSameHemisphere
}


/**
 * Tests whether ‘this’ point is enclosed by the polygon defined by a set of points.
 *
 * @param   {LatLon[]} polygon - Ordered array of points defining vertices of polygon.
 * @returns {bool}     Whether this point is enclosed by polygon.
 *
 * @example
 *   var bounds = [ new LatLon(45,1), new LatLon(45,2), new LatLon(46,2), new LatLon(46,1) ];
 *   var p = new LatLon(45.1, 1.1);
 *   var inside = p.com.sfeatherstone.geodesy.model.spherical.vectors.enclosedBy(bounds); // true
 */
fun LatLon.enclosedBy(polygon_: Array<LatLon>): Boolean {
    // this method uses angle summation test; on a plane, angles for an enclosed point will sum
    // to 360°, angles for an exterior point will sum to 0°. On a sphere, enclosed point angles
    // will sum to less than 360° (due to spherical excess), exterior point angles will be small
    // but non-zero. TODO: are any winding number optimisations applicable to spherical surface?

    // close the polygon so that the last point equals the first point
    val polygon = if(polygon_[0] == polygon_[polygon_.size-1]) polygon_ else polygon_ + arrayOf(polygon_[0])

    val p = this.toVector()

    // get vectors from p to each vertex
    val vectorToVertex = LinkedList<Vector3d>()
    for (value in polygon) vectorToVertex.add(p - value.toVector())
    vectorToVertex.add(vectorToVertex[0])

    // sum subtended angles of each edge (using vector p to determine sign)
    var Σθ = 0.0
    var prevValue : Vector3d? = null
    for (value in vectorToVertex) {
        if (prevValue!=null) Σθ += prevValue.angleTo(value, p)
        prevValue = value
    }

    val enclosed = abs(Σθ) > Math.PI

    return enclosed
}


/**
 * Calculates the area of a spherical polygon where the sides of the polygon are great circle
 * arcs joining the vertices.
 *
 * @param   {LatLon[]} polygon - Array of points defining vertices of the polygon.
 * @param   {number}   [radius=6371e3] - (Mean) radius of earth (defaults to radius in metres).
 * @returns {number}   The area of the polygon in the same units as radius.
 *
 * @example
 *   var polygon = [ new LatLon(0,0), new LatLon(1,0), new LatLon(0,1) ];
 *   var area = LatLon.com.sfeatherstone.geodesy.model.spherical.vectors.areaOf(polygon); // 6.18e9 m²
 */
fun areaOf(polygon_: Array<LatLon>, radius: Double = 6371e3):Double {
    // uses Girard’s theorem: A = [Σθᵢ − (n−2)·π]·R²

    if (polygon_.size < 2) return 0.0
    // close the polygon so that the last point equals the first point
    val polygon = if(polygon_[0] == polygon_[polygon_.size-1]) polygon_ else polygon_ + arrayOf(polygon_[0])

    val n = polygon.size - 1 // number of vertices

    // get great-circle vector for each edge
    val c = LinkedList<Vector3d>()
    var j : Vector3d? = null
    for (v in polygon) {
        val i = v.toVector()
        if (j!=null) c.add(i.cross(j)) // great circle for segment v..v+1
        j = i
    }
    c.add(c[0])

    // sum interior angles; depending on whether polygon is cw or ccw, angle between edges is
    // π−α or π+α, where α is angle between great-circle vectors; so sum α, then take n·π − |Σα|
    // (cannot use Σ(π−|α|) as concave polygons would fail); use vector to 1st point as plane
    // normal for sign of α
    val n1 = polygon[0].toVector()
    var Σα = 0.0
    var prevValue : Vector3d? = null
    for (value in c) {
        if (prevValue!=null) Σα += value.angleTo(prevValue, n1)
        prevValue = value
    }

    val Σθ = n*Math.PI - abs(Σα)

    val E = (Σθ - (n-2)*Math.PI) // spherical excess (in steradians)
    val A = E * radius*radius              // area in units of R²

    return A
}


/**
 * Returns point representing geographic mean of supplied points.
 *
 * @param   {LatLon[]} points - Array of points to be averaged.
 * @returns {LatLon}   Point at the geographic mean of the supplied points.
 * @todo Not yet tested.
 */
fun meanOf(points: Array<LatLon>): LatLon {
    var m = Vector3d(0.0, 0.0, 0.0)

    // add all vectors
    for (element in points) {
        m += element.toVector()
    }

    // m is now geographic mean
    return m.unit().toLatLonS()
}


