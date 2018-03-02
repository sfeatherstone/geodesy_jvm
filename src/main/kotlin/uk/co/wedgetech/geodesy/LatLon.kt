package uk.co.wedgetech.geodesy

import java.lang.Double.isFinite
import java.lang.Double.isNaN
import kotlin.math.sign

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Geodesy tools for an ellipsoidal earth model                       (c) Chris Veness 2005-2016  */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong-convert-coords.html                                     */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-latlon-ellipsoidal.html                     */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


/**
 * Library of geodesy functions for operations on an ellipsoidal earth model.
 *
 * Includes ellipsoid parameters and datums for different coordinate systems, and methods for
 * converting between them and to cartesian coordinates.
 *
 * q.v. Ordnance Survey ‘A guide to coordinate systems in Great Britain’ Section 6
 * www.ordnancesurvey.co.uk/docs/support/guide-coordinate-systems-great-britain.pdf.
 *
 * @module   latlon-ellipsoidal
 * @requires dms
 */


/**
 * Creates lat/lon (polar) point with latitude & longitude values, on a specified datum.
 *
 * @constructor
 * @param {number}       lat - Geodetic latitude in degrees.
 * @param {number}       lon - Longitude in degrees.
 * @param {LatLon.datum} [datum=WGS84] - Datum this point is defined within.
 *
 * @example
 *     var p1 = new LatLon(51.4778, -0.0016, LatLon.datum.WGS84);
 */
class LatLon(val lat: Double, val lon :Double, val datum : Datum = WGS84) {
/*    // allow instantiation without 'new'
    if (!(this instanceof LatLon)) return new LatLon(lat, lon, datum);

    if (datum === undefined) datum = LatLon.datum.WGS84;

    this.lat = Number(lat);
    this.lon = Number(lon);
    this.datum = datum;
}*/

    data class Ellipsoid(val a: Double, val b: Double, val f: Double)

    data class Transform(val tx: Double, val ty: Double, val tz: Double, val s: Double, val rx: Double, val ry: Double, val rz: Double) {
        fun inverse() = Transform(-tx, -ty, -tz, -s, -rx, -ry, -rz)
    }

    data class Datum(val ellipsoid: Ellipsoid, val transform: Transform)

    /**
     * Ellipsoid parameters; major axis (a), minor axis (b), and flattening (f) for each ellipsoid.
     */
    companion object {
        val eWGS84 = Ellipsoid(6378137.0, 6356752.314245, 1 / 298.257223563)
        val eAiry1830 = Ellipsoid(6377563.396, 6356256.909, 1 / 299.3249646)
        val eAiryModified = Ellipsoid(6377340.189, 6356034.448, 1 / 299.3249646)
        val eBessel1841 = Ellipsoid(6377397.155, 6356078.962818, 1 / 299.1528128)
        val eClarke1866 = Ellipsoid(6378206.4, 6356583.8, 1 / 294.978698214)
        val eClarke1880IGN = Ellipsoid(6378249.2, 6356515.0, 1 / 293.466021294)
        val eGRS80 = Ellipsoid(6378137.0, 6356752.314140, 1 / 298.257222101)
        val eIntl1924 = Ellipsoid(6378388.0, 6356911.946, 1 / 297.0) // aka Hayford
        val eWGS72 = Ellipsoid(6378135.0, 6356750.5, 1 / 298.26)

        /**
         * Datums; with associated ellipsoid, and Helmert transform parameters to convert from WGS 84 into
         * given datum.
         *
         * Note that precision of various datums will vary, and WGS-84 (original) is not defined to be
         * accurate to better than ±1 metre. No transformation should be assumed to be accurate to better
         * than a meter; for many datums somewhat less.
         */
        // transforms: t in metres, s in ppm, r in arcseconds                    tx       ty        tz       s        rx       ry       rz
        @JvmField val ED50 = Datum(eIntl1924, Transform(89.5, 93.8, 123.1, -1.2, 0.0, 0.0, 0.156))
        @JvmField val Irl1975 = Datum(eAiryModified, Transform(-482.530, 130.596, -564.557, -8.150, -1.042, -0.214, -0.631))
        @JvmField val NAD27 = Datum(eClarke1866, Transform(8.0, -160.0, -176.0, 0.0, 0.0, 0.0, 0.0))
        @JvmField val NAD83 = Datum(eGRS80, Transform(1.004, -1.910, -0.515, -0.0015, 0.0267, 0.00034, 0.011))
        @JvmField val NTF = Datum(eClarke1880IGN, Transform(168.0, 60.0, -320.0, 0.0, 0.0, 0.0, 0.0))
        @JvmField val OSGB36 = Datum(eAiry1830, Transform(-446.448, 125.157, -542.060, 20.4894, -0.1502, -0.2470, -0.8421))
        @JvmField val Potsdam = Datum(eBessel1841, Transform(-582.0, -105.0, -414.0, -8.3, 1.04, 0.35, -3.08))
        @JvmField val TokyoJapan = Datum(eBessel1841, Transform(148.0, -507.0, -685.0, 0.0, 0.0, 0.0, 0.0))
        @JvmField val WGS72 = Datum(eWGS72, Transform(0.0, 0.0, -4.5, -0.22, 0.0, 0.0, 0.554))
        @JvmField val WGS84 = Datum(eWGS84, Transform(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

        /* sources:
     * - ED50:          www.gov.uk/guidance/oil-and-gas-petroleum-operations-notices#pon-4
     * - Irl1975:       www.osi.ie/wp-content/uploads/2015/05/transformations_booklet.pdf
     *   ... note: many sources have opposite sign to rotations - to be checked!
     * - NAD27:         en.wikipedia.org/wiki/Helmert_transformation
     * - NAD83: (2009); www.uvm.edu/giv/resources/WGS84_NAD83.pdf
     *   ... note: functionally ≡ WGS84 - if you *really* need to convert WGS84<->NAD83, you need more knowledge than this!
     * - NTF:           Nouvelle Triangulation Francaise geodesie.ign.fr/contenu/fichiers/Changement_systeme_geodesique.pdf
     * - OSGB36:        www.ordnancesurvey.co.uk/docs/support/guide-coordinate-systems-great-britain.pdf
     * - Potsdam:       kartoweb.itc.nl/geometrics/Coordinate%20transformations/coordtrans.html
     * - TokyoJapan:    www.geocachingtoolbox.com?page=datumEllipsoidDetails
     * - WGS72:         www.icao.int/safety/pbn/documentation/eurocontrol/eurocontrol wgs 84 implementation manual.pdf
     *
     * more transform parameters are available from earth-info.nga.mil/GandG/coordsys/datums/NATO_DT.pdf,
     * www.fieldenmaps.info/cconv/web/cconv_params.js
     */
    }


    /**
     * Converts ‘this’ lat/lon coordinate to new coordinate system.
     *
     * @param   {LatLon.datum} toDatum - Datum this coordinate is to be converted to.
     * @returns {LatLon} This point converted to new datum.
     *
     * @example
     *     var pWGS84 = new LatLon(51.4778, -0.0016, LatLon.datum.WGS84);
     *     var pOSGB = pWGS84.convertDatum(LatLon.datum.OSGB36); // 51.4773°N, 000.0000°E
     */
    fun convertDatum(toDatum: Datum): LatLon {
        var oldLatLon = this;
        var transform :  Transform? = null

        if (oldLatLon.datum == WGS84) {
            // converting from WGS 84
            transform = toDatum.transform;
        }
        if (toDatum == WGS84) {
            // converting to WGS 84; use inverse transform (don't overwrite original!)
            transform = oldLatLon.datum.transform.inverse();
        }
        if (transform == null) {
            // neither this.datum nor toDatum are WGS84: convert this to WGS84 first
            oldLatLon = this.convertDatum(WGS84);
            transform = toDatum.transform;
        }

        val oldCartesian = oldLatLon.toCartesian()                // convert polar to cartesian...
        val newCartesian = oldCartesian.applyTransform(transform) // ...apply transform...
        val newLatLon = newCartesian.toLatLonE(toDatum)           // ...and convert cartesian to polar

        return newLatLon;
    };


    /**
     * Converts ‘this’ point from (geodetic) latitude/longitude coordinates to (geocentric) cartesian
     * (x/y/z) coordinates.
     *
     * @returns {Vector3d} Vector pointing to lat/lon point, with x, y, z in metres from earth centre.
     */
    fun toCartesian(): Vector3d {
        val φ = Math.toRadians(lat)
        val λ = Math.toRadians(lon)
        val h = 0  // height above ellipsoid - not currently used
        val a = this.datum.ellipsoid.a
        val f = this.datum.ellipsoid.f

        val sinφ = Math.sin(φ)
        val cosφ = Math.cos(φ)
        val sinλ = Math.sin(λ)
        val cosλ = Math.cos(λ)

        val eSq = 2 * f - f * f                      // 1st eccentricity squared ≡ (a²-b²)/a²
        val ν = a / Math.sqrt(1 - eSq * sinφ * sinφ) // radius of curvature in prime vertical

        val x = (ν + h) * cosφ * cosλ
        val y = (ν + h) * cosφ * sinλ
        val z = (ν * (1 - eSq) + h) * sinφ

        return Vector3d(x, y, z)
    };


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
    fun distanceTo(point :LatLon, radius : Double = 6371e3):Double {

        // a = sin²(Δφ/2) + cos(φ1)⋅cos(φ2)⋅sin²(Δλ/2)
        // tanδ = √(a) / √(1−a)
        // see mathforum.org/library/drmath/view/51879.html for derivation

        val R = radius
        val φ1 = this.lat.toRadians()
        val λ1 = this.lon.toRadians();
        val φ2 = point.lat.toRadians()
        val λ2 = point.lon.toRadians();
        val Δφ = φ2 - φ1;
        val Δλ = λ2 - λ1;

        val a = Math.sin(Δφ/2) * Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val d = R * c

        return d;
    };


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
    fun bearingTo(point : LatLon): Double {

        // tanθ = sinΔλ⋅cosφ2 / cosφ1⋅sinφ2 − sinφ1⋅cosφ2⋅cosΔλ
        // see mathforum.org/library/drmath/view/55417.html for derivation

        val φ1 = this.lat.toRadians()
        val φ2 = point.lat.toRadians()
        val Δλ = (point.lon-this.lon).toRadians()
        val y = Math.sin(Δλ) * Math.cos(φ2)
        val x = Math.cos(φ1)*Math.sin(φ2) -
                Math.sin(φ1)*Math.cos(φ2)*Math.cos(Δλ)
        val θ = Math.atan2(y, x)

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
    fun finalBearingTo(point: LatLon):Double {
        // get initial bearing from destination point to this point & reverse it by adding 180°
        return ( point.bearingTo(this)+180 ) % 360;
    };


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
    fun midpointTo(point: LatLon): LatLon {

        // φm = atan2( sinφ1 + sinφ2, √( (cosφ1 + cosφ2⋅cosΔλ) ⋅ (cosφ1 + cosφ2⋅cosΔλ) ) + cos²φ2⋅sin²Δλ )
        // λm = λ1 + atan2(cosφ2⋅sinΔλ, cosφ1 + cosφ2⋅cosΔλ)
        // see mathforum.org/library/drmath/view/51822.html for derivation

        val φ1 = this.lat.toRadians()
        val λ1 = this.lon.toRadians();
        val φ2 = point.lat.toRadians();
        val Δλ = (point.lon-this.lon).toRadians();

        val Bx = Math.cos(φ2) * Math.cos(Δλ);
        val By = Math.cos(φ2) * Math.sin(Δλ);

        val x = Math.sqrt((Math.cos(φ1) + Bx) * (Math.cos(φ1) + Bx) + By * By);
        val y = Math.sin(φ1) + Math.sin(φ2);
        val φ3 = Math.atan2(y, x);

        val λ3 = λ1 + Math.atan2(By, Math.cos(φ1) + Bx);

        return LatLon(φ3.toDegrees(), (λ3.toDegrees()+540)%360-180); // normalise to −180..+180°
    };


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
    fun intermediatePointTo(point :LatLon, fraction: Double): LatLon {
        val φ1 = this.lat.toRadians()
        val λ1 = this.lon.toRadians()
        val φ2 = point.lat.toRadians()
        val λ2 = point.lon.toRadians()
        val sinφ1 = Math.sin(φ1)
        val cosφ1 = Math.cos(φ1)
        val sinλ1 = Math.sin(λ1)
        val cosλ1 = Math.cos(λ1)
        val sinφ2 = Math.sin(φ2)
        val cosφ2 = Math.cos(φ2)
        val sinλ2 = Math.sin(λ2)
        val cosλ2 = Math.cos(λ2)

        // distance between points
        val Δφ = φ2 - φ1;
        val Δλ = λ2 - λ1;
        val a = Math.sin(Δφ/2) * Math.sin(Δφ/2)
        + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        val δ = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        val A = Math.sin((1-fraction)*δ) / Math.sin(δ);
        val B = Math.sin(fraction*δ) / Math.sin(δ);

        val x = A * cosφ1 * cosλ1 + B * cosφ2 * cosλ2;
        val y = A * cosφ1 * sinλ1 + B * cosφ2 * sinλ2;
        val z = A * sinφ1 + B * sinφ2;

        val φ3 = Math.atan2(z, Math.sqrt(x*x + y*y));
        val λ3 = Math.atan2(y, x);

        return LatLon(φ3.toDegrees(), (λ3.toDegrees()+540)%360-180); // normalise lon to −180..+180°
    };


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
    fun destinationPoint(distance: Double, bearing: Double, radius :Double = 6371e3): LatLon {
        // sinφ2 = sinφ1⋅cosδ + cosφ1⋅sinδ⋅cosθ
        // tanΔλ = sinθ⋅sinδ⋅cosφ1 / cosδ−sinφ1⋅sinφ2
        // see mathforum.org/library/drmath/view/52049.html for derivation

        var δ = distance / radius // angular distance in radians
        var θ = bearing.toRadians()

        var φ1 = this.lat.toRadians();
        var λ1 = this.lon.toRadians();

        var sinφ1 = Math.sin(φ1)
        val cosφ1 = Math.cos(φ1);
        var sinδ = Math.sin(δ)
        val cosδ = Math.cos(δ);
        var sinθ = Math.sin(θ)
        val cosθ = Math.cos(θ);

        var sinφ2 = sinφ1*cosδ + cosφ1*sinδ*cosθ;
        var φ2 = Math.asin(sinφ2);
        var y = sinθ * sinδ * cosφ1;
        var x = cosδ - sinφ1 * sinφ2;
        var λ2 = λ1 + Math.atan2(y, x);

        return LatLon(φ2.toDegrees(), (λ2.toDegrees()+540)%360-180); // normalise to −180..+180°
    };


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

        var φ1 = p1.lat.toRadians()
        val λ1 = p1.lon.toRadians();
        var φ2 = p2.lat.toRadians()
        val λ2 = p2.lon.toRadians();
        var θ13 = bearing1.toRadians()
        val θ23 = bearing2.toRadians();
        var Δφ = φ2-φ1
        val Δλ = λ2-λ1;

        // angular distance p1-p2
        var δ12 = 2*Math.asin( Math.sqrt( Math.sin(Δφ/2)*Math.sin(Δφ/2)
                + Math.cos(φ1)*Math.cos(φ2)*Math.sin(Δλ/2)*Math.sin(Δλ/2) ) );

        if (δ12 == 0.0) return null;

        // initial/final bearings between points
        var θa = Math.acos( ( Math.sin(φ2) - Math.sin(φ1)*Math.cos(δ12) ) / ( Math.sin(δ12)*Math.cos(φ1) ) );
        if (isNaN(θa)) θa = 0.0; // protect against rounding
        var θb = Math.acos( ( Math.sin(φ1) - Math.sin(φ2)*Math.cos(δ12) ) / ( Math.sin(δ12)*Math.cos(φ2) ) );

        var θ12 = if (Math.sin(λ2-λ1)>0) θa else 2*Math.PI-θa
        var θ21 = if (Math.sin(λ2-λ1)>0) 2*Math.PI-θb else θb

        var α1 = θ13 - θ12; // angle 2-1-3
        var α2 = θ21 - θ23; // angle 1-2-3

        if (Math.sin(α1)==0.0 && Math.sin(α2)==0.0) return null; // infinite intersections
        if (Math.sin(α1)*Math.sin(α2) < 0.0) return null;      // ambiguous intersection

        var α3 = Math.acos( -Math.cos(α1)*Math.cos(α2) + Math.sin(α1)*Math.sin(α2)*Math.cos(δ12) );
        var δ13 = Math.atan2( Math.sin(δ12)*Math.sin(α1)*Math.sin(α2), Math.cos(α2)+Math.cos(α1)*Math.cos(α3) );
        var φ3 = Math.asin( Math.sin(φ1)*Math.cos(δ13) + Math.cos(φ1)*Math.sin(δ13)*Math.cos(θ13) );
        var Δλ13 = Math.atan2( Math.sin(θ13)*Math.sin(δ13)*Math.cos(φ1), Math.cos(δ13)-Math.sin(φ1)*Math.sin(φ3) );
        var λ3 = λ1 + Δλ13;

        return LatLon(φ3.toDegrees(), (λ3.toDegrees()+540)%360-180); // normalise to −180..+180°
    };


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
    fun crossTrackDistanceTo(pathStart: LatLon, pathEnd: LatLon, radius: Double = 6371e3): Double {
        var δ13 = pathStart.distanceTo(this, radius) / radius;
        var θ13 = pathStart.bearingTo(this).toRadians();
        var θ12 = pathStart.bearingTo(pathEnd).toRadians();

        var δxt = Math.asin(Math.sin(δ13) * Math.sin(θ13-θ12));

        return δxt * radius;
    };


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
    fun alongTrackDistanceTo(pathStart :LatLon, pathEnd :LatLon, radius :Double = 6371e3): Double {
        var δ13 = pathStart.distanceTo(this, radius) / radius;
        var θ13 = pathStart.bearingTo(this).toRadians();
        var θ12 = pathStart.bearingTo(pathEnd).toRadians();

        var δxt = Math.asin(Math.sin(δ13) * Math.sin(θ13-θ12));

        var δat = Math.acos(Math.cos(δ13) / Math.abs(Math.cos(δxt)));

        return δat*(Math.cos(θ12-θ13)).sign * radius;
    };


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
    fun maxLatitude(bearing: Double): Double {
        var θ = bearing.toRadians();

        var φ = this.lat.toRadians();

        var φMax = Math.acos(Math.abs(Math.sin(θ)*Math.cos(φ)));

        return φMax.toDegrees();
    };


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
        var φ = latitude.toRadians();

        var φ1 = point1.lat.toRadians();
        var λ1 = point1.lon.toRadians();
        var φ2 = point2.lat.toRadians();
        var λ2 = point2.lon.toRadians();

        var Δλ = λ2 - λ1;

        var x = Math.sin(φ1) * Math.cos(φ2) * Math.cos(φ) * Math.sin(Δλ);
        var y = Math.sin(φ1) * Math.cos(φ2) * Math.cos(φ) * Math.cos(Δλ) - Math.cos(φ1) * Math.sin(φ2) * Math.cos(φ);
        var z = Math.cos(φ1) * Math.cos(φ2) * Math.sin(φ) * Math.sin(Δλ);

        if (z*z > x*x + y*y) return null; // great circle doesn't reach latitude

        var λm = Math.atan2(-y, x);                  // longitude at max latitude
        var Δλi = Math.acos(z / Math.sqrt(x*x+y*y)); // Δλ from λm to intersection points

        var λi1 = λ1 + λm - Δλi;
        var λi2 = λ1 + λm + Δλi;

        return Pair((λi1.toDegrees()+540)%360-180, (λi2.toDegrees()+540)%360-180) // normalise to −180..+180°
    };


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
    fun rhumbDistanceTo(point: LatLon, radius: Double = 6371e3): Double {
        // see www.edwilliams.org/avform.htm#Rhumb

        var R = radius;
        var φ1 = this.lat.toRadians()
        val φ2 = point.lat.toRadians();
        var Δφ = φ2 - φ1;
        var Δλ = Math.abs(point.lon-this.lon).toRadians();
        // if dLon over 180° take shorter rhumb line across the anti-meridian:
        if (Δλ > Math.PI) Δλ -= 2*Math.PI;

        // on Mercator projection, longitude distances shrink by latitude; q is the 'stretch factor'
        // q becomes ill-conditioned along E-W line (0/0); use empirical tolerance to avoid it
        var Δψ = Math.log(Math.tan(φ2/2+Math.PI/4)/Math.tan(φ1/2+Math.PI/4));
        var q = if (Math.abs(Δψ) > 10e-12) Δφ/Δψ else Math.cos(φ1)

        // distance is pythagoras on 'stretched' Mercator projection
        var δ = Math.sqrt(Δφ*Δφ + q*q*Δλ*Δλ); // angular distance in radians
        var dist = δ * R;

        return dist;
    };


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
    fun rhumbBearingTo(point: LatLon): Double {

        var φ1 = this.lat.toRadians()
        val φ2 = point.lat.toRadians();
        var Δλ = (point.lon-this.lon).toRadians();
        // if dLon over 180° take shorter rhumb line across the anti-meridian:
        if (Δλ >  Math.PI) Δλ -= 2*Math.PI;
        if (Δλ < -Math.PI) Δλ += 2*Math.PI;

        var Δψ = Math.log(Math.tan(φ2/2+Math.PI/4)/Math.tan(φ1/2+Math.PI/4));

        var θ = Math.atan2(Δλ, Δψ);

        return (θ.toDegrees()+360) % 360;
    };


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
    fun rhumbDestinationPoint(distance: Double, bearing: Double, radius: Double = 6371e3): LatLon {
        var δ = distance / radius; // angular distance in radians
        var φ1 = this.lat.toRadians()
        val λ1 = this.lon.toRadians();
        var θ = bearing.toRadians();

        var Δφ = δ * Math.cos(θ);
        var φ2 = φ1 + Δφ;

        // check for some daft bugger going past the pole, normalise latitude if so
        if (Math.abs(φ2) > Math.PI/2){
            φ2 = if (φ2>0) Math.PI-φ2 else -Math.PI-φ2
        }

        var Δψ = Math.log(Math.tan(φ2/2+Math.PI/4)/Math.tan(φ1/2+Math.PI/4));
        var q = if (Math.abs(Δψ) > 10e-12) Δφ / Δψ else Math.cos(φ1); // E-W course becomes ill-conditioned with 0/0

        var Δλ = δ*Math.sin(θ)/q;
        var λ2 = λ1 + Δλ;

        return LatLon(φ2.toDegrees(), (λ2.toDegrees()+540.0) % 360.0 - 180.0); // normalise to −180..+180°
    };


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
    fun rhumbMidpointTo(point: LatLon): LatLon {

        // see mathforum.org/kb/message.jspa?messageID=148837

        var φ1 = this.lat.toRadians()
        var λ1 = this.lon.toRadians();
        var φ2 = point.lat.toRadians()
        val λ2 = point.lon.toRadians();

        if (Math.abs(λ2-λ1) > Math.PI) λ1 += 2*Math.PI; // crossing anti-meridian

        var φ3 = (φ1+φ2)/2;
        var f1 = Math.tan(Math.PI/4 + φ1/2);
        var f2 = Math.tan(Math.PI/4 + φ2/2);
        var f3 = Math.tan(Math.PI/4 + φ3/2);
        var λ3 = ( (λ2-λ1)*Math.log(f3) + λ1*Math.log(f2) - λ2*Math.log(f1) ) / Math.log(f2/f1);

        if (!isFinite(λ3)) λ3 = (λ1+λ2)/2; // parallel of latitude

        return LatLon(φ3.toDegrees(), (λ3.toDegrees()+540)%360-180); // normalise to −180..+180°
    };


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
     *   var area = LatLon.areaOf(polygon); // 6.18e9 m²
     */
    fun areaOf(polygonInput: Array<LatLon>, radius: Double = 6371e3):Double {
        // uses method due to Karney: osgeo-org.1560.x6.nabble.com/Area-of-a-spherical-polygon-td3841625.html;
        // for each edge of the polygon, tan(E/2) = tan(Δλ/2)·(tan(φ1/2) + tan(φ2/2)) / (1 + tan(φ1/2)·tan(φ2/2))
        // where E is the spherical excess of the trapezium obtained by extending the edge to the equator

        if (polygonInput.size < 3) return 0.0

        // close polygon so that last point equals first point
        val closed = polygonInput[0]==polygonInput[polygonInput.size-1]
        val polygon = if (closed) polygonInput else Array<LatLon>(polygonInput.size+1, { if (it < polygonInput.size) polygonInput[it] else polygonInput[0]})

        if (polygon.size < 4) return 0.0

        val nVertices = polygon.size - 1;

        var S = 0.0 // spherical excess in steradians
        for (v in 0..nVertices) {
            var φ1 = polygon[v].lat.toRadians();
            var φ2 = polygon[v+1].lat.toRadians();
            var Δλ = (polygon[v+1].lon - polygon[v].lon).toRadians();
            var E = 2 * Math.atan2(Math.tan(Δλ/2) * (Math.tan(φ1/2)+Math.tan(φ2/2)), 1 + Math.tan(φ1/2)*Math.tan(φ2/2));
            S += E;
        }

        // returns whether polygon encloses pole: sum of course deltas around pole is 0° rather than
        // normal ±360°: blog.element84.com/determining-if-a-spherical-polygon-contains-a-pole.html
        fun isPoleEnclosedBy(polygon: Array<LatLon>):Boolean {
            // TODO: any better test than this?
            var ΣΔ = 0.0
            var prevBrng = polygon[0].bearingTo(polygon[1]);
            for (v in 0..polygon.size-1) {
                var initBrng = polygon[v].bearingTo(polygon[v+1]);
                var finalBrng = polygon[v].finalBearingTo(polygon[v+1]);
                ΣΔ += (initBrng - prevBrng + 540.0) % 360.0 - 180.0
                ΣΔ += (finalBrng - initBrng + 540.0) % 360.0 - 180.0
                prevBrng = finalBrng;
            }
            var initBrng = polygon[0].bearingTo(polygon[1]);
            ΣΔ += (initBrng - prevBrng + 540) % 360 - 180;
            // TODO: fix (intermittant) edge crossing pole - eg (85,90), (85,0), (85,-90)
            var enclosed = Math.abs(ΣΔ) < 90; // 0°-ish
            return enclosed;
        }

        if (isPoleEnclosedBy(polygon)) S = Math.abs(S) - 2*Math.PI;

        var A = Math.abs(S * radius*radius); // area in units of R

        return A;
    };


    /**
     * Returns a string representation of ‘this’ point, formatted as degrees, degrees+minutes, or
     * degrees+minutes+seconds.
     *
     * @param   {string} [format=dms] - Format point as 'd', 'dm', 'dms'.
     * @param   {number} [dp=0|2|4] - Number of decimal places to use - default 0 for dms, 2 for dm, 4 for d.
     * @returns {string} Comma-separated latitude/longitude.
     */
    fun toString(format: String, dp: Int = 0) : String
    {
        return "${this.lat.toLatitude(format, dp)}, ${this.lon.toLongitude(format, dp)}"
    }



}
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */




