package com.sfeatherstone.geodesy.vectors

import com.sfeatherstone.geodesy.LatLonDatum
import com.sfeatherstone.geodesy.format
import com.sfeatherstone.geodesy.toDegrees
import com.sfeatherstone.geodesy.toRadians
import kotlin.math.sign

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Vector handling functions                                          (c) Chris Veness 2011-2016  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-vector3d.html                               */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


/**
 * Library of 3-d vector manipulation routines.
 *
 * In a geodesy context, these vectors may be used to represent:
 *  - n-vector representing a normal to point on Earth's surface
 *  - earth-centered, earth fixed vector (≡ Gade’s ‘p-vector’)
 *  - great circle normal to vector (on spherical earth model)
 *  - motion vector on Earth's surface
 *  - etc
 *
 * Functions return vectors as return results, so that operations can be chained.
 * @example var v = v1.cross(v2).dot(v3) // ≡ v1×v2⋅v3
 *
 * @module vector3d
 */


/**
 * Creates a 3-d vector.
 *
 * The vector may be normalised, or use x/y/z values for eg height relative to the sphere or
 * ellipsoid, distance from earth centre, etc.
 *
 * @constructor
 * @param {number} x - X component of vector.
 * @param {number} y - Y component of vector.
 * @param {number} z - Z component of vector.
 */
data class Vector3d(val x: Double , val y : Double , val z : Double) {


    /**
     * Adds supplied vector to ‘this’ vector.
     *
     * @param   {Vector3d} v - Vector to be added to this vector.
     * @returns {Vector3d} Vector representing sum of this and v.
     */
    operator fun plus(v : Vector3d) = Vector3d(this.x + v.x, this.y + v.y, this.z + v.z)


    /**
     * Subtracts supplied vector from ‘this’ vector.
     *
     * @param   {Vector3d} v - Vector to be subtracted from this vector.
     * @returns {Vector3d} Vector representing difference between this and v.
     */
    operator fun minus(v : Vector3d) = Vector3d(this.x - v.x, this.y - v.y, this.z - v.z)

    /**
     * Multiplies ‘this’ vector by a scalar value.
     *
     * @param   {number}   x - Factor to multiply this vector by.
     * @returns {Vector3d} Vector scaled by x.
     */
    operator fun times(x :Double) = Vector3d(this.x * x, this.y * x, this.z * x)


    /**
     * Divides ‘this’ vector by a scalar value.
     *
     * @param   {number}   x - Factor to divide this vector by.
     * @returns {Vector3d} Vector divided by x.
     */
    operator fun div(x: Double) = Vector3d(this.x / x, this.y / x, this.z / x)


    /**
     * Multiplies ‘this’ vector by the supplied vector using dot (scalar) product.
     *
     * @param   {Vector3d} v - Vector to be dotted with this vector.
     * @returns {number} Dot product of ‘this’ and v.
     */
    fun dot(v: Vector3d): Double = this.x * v.x + this.y * v.y + this.z * v.z

    /**
     * Multiplies ‘this’ vector by the supplied vector using cross (vector) product.
     *
     * @param   {Vector3d} v - Vector to be crossed with this vector.
     * @returns {Vector3d} Cross product of ‘this’ and v.
     */
    fun cross(v: Vector3d): Vector3d
    {
        val x = this.y * v.z - this.z * v.y
        val y = this.z * v.x - this.x * v.z
        val z = this.x * v.y - this.y * v.x

        return Vector3d(x, y, z)
    }


    /**
     * Negates a vector to point in the opposite direction
     *
     * @returns {Vector3d} Negated vector.
     */
    val negate by lazy({ Vector3d(-this.x, -this.y, -this.z) })


    /**
     * Length (magnitude or norm) of ‘this’ vector
     *
     * @returns {number} Magnitude of this vector.
     */
    var length : Double = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z)


    /**
     * Normalizes a vector to its unit vector
     * – if the vector is already unit or is zero magnitude, this is a no-op.
     *
     * @returns {Vector3d} Normalised version of this vector.
     */
    fun unit() : Vector3d {
        val norm = this.length
        if (norm == 1.0) return this
        if (norm == 0.0) return this

        val x = this.x / norm
        val y = this.y / norm
        val z = this.z / norm

        return Vector3d(x, y, z)
    }


    /**
     * Calculates the angle between ‘this’ vector and supplied vector.
     *
     * @param   {Vector3d} v
     * @param   {Vector3d} [n] - Plane normal: if supplied, angle is -π..+π, signed +ve if this->v is
     *     clockwise looking along n, -ve in opposite direction (if not supplied, angle is always 0..π).
     * @returns {number} Angle (in radians) between this vector and supplied vector.
     */
    fun angleTo(v : Vector3d, n : Vector3d = Vector3d(0.0, 0.0, 0.0)) :Double {
        val sign = if (n == Vector3d(0.0, 0.0, 0.0)) 1.0 else this.cross(v).dot(n).sign
        val sinθ = this.cross(v).length * sign
        val cosθ = this.dot(v)

        return Math.atan2(sinθ, cosθ)
    }


    /**
     * Rotates ‘this’ point around an axis by a specified angle.
     *
     * @param   {Vector3d} axis - The axis being rotated around.
     * @param   {number}   theta - The angle of rotation (in radians).
     * @returns {Vector3d} The rotated point.
     */
    fun rotateAround(axis: Vector3d, thetaRadians: Double ): Vector3d
    {
        // en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle
        // en.wikipedia.org/wiki/Quaternions_and_spatial_rotation#Quaternion-derived_rotation_matrix
        val p1 = this.unit()
        val p = arrayOf(p1.x, p1.y, p1.z) // the point being rotated
        val a = axis.unit()          // the axis being rotated around
        val s = Math.sin(thetaRadians)
        val c = Math.cos(thetaRadians)
        // quaternion-derived rotation matrix
        val q = arrayOf(
                arrayOf(a.x * a.x * (1 - c) + c, a.x * a.y * (1 - c) - a.z * s, a.x * a.z * (1 - c) + a.y * s),
                arrayOf(a.y * a.x * (1 - c) + a.z * s, a.y * a.y * (1 - c) + c, a.y * a.z * (1 - c) - a.x * s),
                arrayOf(a.z * a.x * (1 - c) - a.y * s, a.z * a.y * (1 - c) + a.x * s, a.z * a.z * (1 - c) + c))
        // multiply q × p
        val qp = arrayOf(0.0, 0.0, 0.0)
        for (i in 0..2) {
            for (j in 0..2) {
                qp[i] += q[i][j] * p[j]
            }
        }
        return Vector3d(qp[0], qp[1], qp[2])
        // qv en.wikipedia.org/wiki/Rodrigues'_rotation_formula...
    }

    /**
     * Converts ‘this’ (geocentric) cartesian (x/y/z) point to (ellipsoidal geodetic) latitude/longitude
     * coordinates on specified datum.
     *
     * Uses Bowring’s (1985) formulation for μm precision in concise form.
     *
     * @param {LatLon.datum.transform} datum - Datum to use when converting point.
     */
    fun toLatLonE(datum: LatLonDatum.Datum): LatLonDatum {
        val a = datum.ellipsoid.a
        val b = datum.ellipsoid.b
        val f = datum.ellipsoid.f

        val e2 = 2*f - f*f   // 1st eccentricity squared ≡ (a²-b²)/a²
        val ε2 = e2 / (1-e2) // 2nd eccentricity squared ≡ (a²-b²)/b²
        val p = Math.sqrt(x*x + y*y) // distance from minor axis
        val R = Math.sqrt(p*p + z*z) // polar radius

        // parametric latitude (Bowring eqn 17, replacing tanβ = z·a / p·b)
        val tanβ = (b*z)/(a*p) * (1+ε2*b/R)
        val sinβ = tanβ / Math.sqrt(1+tanβ*tanβ)
        val cosβ = sinβ / tanβ

        // geodetic latitude (Bowring eqn 18: tanφ = z+ε²bsin³β / p−e²cos³β)
        val φ = if (Double.NaN == cosβ) 0.0 else Math.atan2(z + ε2*b*sinβ*sinβ*sinβ, p - e2*a*cosβ*cosβ*cosβ)

        // longitude
        val λ = Math.atan2(y, x)

        // height above ellipsoid (Bowring eqn 7) [not currently used]
        val sinφ = Math.sin(φ)
        val cosφ = Math.cos(φ)
        val ν = a/Math.sqrt(1-e2*sinφ*sinφ) // length of the normal terminated by the minor axis
        var h = p*cosφ + z*sinφ - (a*a/ν)

        return LatLonDatum(φ.toDegrees(), λ.toDegrees(), datum)
    }

    /**
     * Applies Helmert transform to ‘this’ point using transform parameters t.
     *
     * @private
     * @param   {number[]} t - Transform to apply to this point.
     * @returns {Vector3} Transformed point.
     */
    fun applyTransform(t: LatLonDatum.Transform) : Vector3d {
        // this point
        val x1 = this.x
        val y1 = this.y
        val z1 = this.z

        // transform parameters
        val s1 = t.s/1e6 + 1            // scale: normalise parts-per-million to (s+1)
        val rx = (t.rx/3600).toRadians() // x-rotation: normalise arcseconds to radians
        val ry = (t.ry/3600).toRadians() // y-rotation: normalise arcseconds to radians
        val rz = (t.rz/3600).toRadians() // z-rotation: normalise arcseconds to radians

        // apply transform
        val x2 = t.tx + x1*s1 - y1*rz + z1*ry
        val y2 = t.ty + x1*rz + y1*s1 - z1*rx
        val z2 = t.tz - x1*ry + y1*rx + z1*s1

        return Vector3d(x2, y2, z2)
    }

    /**
     * String representation of vector.
     *
     * @param   {number} [precision=3] - Number of decimal places to be used.
     * @returns {string} Vector represented as [x,y,z].
     */
    fun toString(precision : Int = 3) ="[${this.x.format(precision)},${this.y.format(precision)},${this.z.format(precision)}]"



}