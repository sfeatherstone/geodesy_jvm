package uk.co.wedgetech.geodesy
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
    };

}
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */




