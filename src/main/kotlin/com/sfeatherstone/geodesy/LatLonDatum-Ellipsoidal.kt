package com.sfeatherstone.geodesy


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
fun LatLonDatum.convertDatum(toDatum: LatLonDatum.Datum): LatLonDatum {
    var oldLatLon = this
    var transform :  LatLonDatum.Transform? = null

    if (oldLatLon.datum == LatLonDatum.WGS84) {
        // converting from WGS 84
        transform = toDatum.transform
    }
    if (toDatum == LatLonDatum.WGS84) {
        // converting to WGS 84; use inverse transform (don't overwrite original!)
        transform = oldLatLon.datum.transform.inverse()
    }
    if (transform == null) {
        // neither this.datum nor toDatum are WGS84: convert this to WGS84 first
        oldLatLon = this.convertDatum(LatLonDatum.WGS84)
        transform = toDatum.transform
    }

    val oldCartesian = oldLatLon.toCartesian()                // convert polar to cartesian...
    val newCartesian = oldCartesian.applyTransform(transform) // ...apply transform...
    val newLatLon = newCartesian.toLatLonE(toDatum)           // ...and convert cartesian to polar

    return newLatLon
}


/**
 * Converts ‘this’ point from (geodetic) latitude/longitude coordinates to (geocentric) cartesian
 * (x/y/z) coordinates.
 *
 * @returns {Vector3d} Vector pointing to lat/lon point, with x, y, z in metres from earth centre.
 */
fun LatLonDatum.toCartesian(): Vector3d {
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
}

