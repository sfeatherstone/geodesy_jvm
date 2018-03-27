package com.sfeatherstone.geodesy

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Geodesy tools for an ellipsoidal earth model                       (c) Chris Veness 2005-2016  */
/*                                                                        Simon Featherstone 2018 */
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
 * lat/lon (polar) point with latitude & longitude values
 *
 * @property        lat - Geodetic latitude in degrees.
 * @property        lon - Longitude in degrees.
 * @property        datum=WGS84 - Datum this point is defined within.
 *
 * @example
 *     val p1 = new LatLon(51.4778, -0.0016);
 */
data class LatLon(val lat: Double, val lon :Double, val datum : Datum = WGS84, val convergence: Double? = null, val scale:Double? = null) {

    override fun toString() = toString("dms", 0)
    /**
     * Returns a string representation of ‘this’ point, formatted as degrees, degrees+minutes, or
     * degrees+minutes+seconds.
     *
     * @param   format=dms - Format point as 'd', 'dm', 'dms'.
     * @param   dp=0|2|4 - Number of decimal places to use - default 0 for dms, 2 for dm, 4 for d.
     * @returns Comma-separated latitude/longitude.
     */
    fun toString(format: String, dp: Int? = null) : String
    {
        return "${this.lat.toLatitude(format, dp)}, ${this.lon.toLongitude(format, dp)}"
    }

    /**
     * Checks if another point is equal to ‘this’ point.
     *
     * @param   {LatLon} point - Point to be compared against this point.
     * @returns {bool}    True if points are identical.
     *
     * @example
     *   var p1 = new LatLon(52.205, 0.119);
     *   var p2 = new LatLon(52.205, 0.119);
     *   var equal = p1.equals(p2); // true
     */
/*    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is LatLon) return false
        return obj.lat == lat && obj.lon == lon
    }*/


}
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */




