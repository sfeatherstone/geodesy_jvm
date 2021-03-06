@file:JvmName("Datums")
package com.sfeatherstone.geodesy

/**
 * Ellipsoid parameters; major axis (a), minor axis (b), and flattening (f) for each ellipsoid.
 * @property a major axis
 * @property b minor axis
 * @property f flattening
 */
data class Ellipsoid(val a: Double, val b: Double, val f: Double)

data class Transform(val tx: Double, val ty: Double, val tz: Double, val s: Double, val rx: Double, val ry: Double, val rz: Double) {
    fun inverse() = Transform(-tx, -ty, -tz, -s, -rx, -ry, -rz)
}

/**
 * Datums; with associated ellipsoid, and Helmert transform parameters to convert from WGS 84 into
 * given datum.
 *
 * @property ellipsoid
 * @property transform
 */
data class Datum(val ellipsoid: Ellipsoid, val transform: Transform)

/**
 * Ellipsoid parameters; major axis (a), minor axis (b), and flattening (f) for each ellipsoid.
 */
internal val eWGS84 = Ellipsoid(6378137.0, 6356752.314245, 1 / 298.257223563)
internal val eAiry1830 = Ellipsoid(6377563.396, 6356256.909, 1 / 299.3249646)
internal val eAiryModified = Ellipsoid(6377340.189, 6356034.448, 1 / 299.3249646)
internal val eBessel1841 = Ellipsoid(6377397.155, 6356078.962818, 1 / 299.1528128)
internal val eClarke1866 = Ellipsoid(6378206.4, 6356583.8, 1 / 294.978698214)
internal val eClarke1880IGN = Ellipsoid(6378249.2, 6356515.0, 1 / 293.466021294)
internal val eGRS80 = Ellipsoid(6378137.0, 6356752.314140, 1 / 298.257222101)
internal val eIntl1924 = Ellipsoid(6378388.0, 6356911.946, 1 / 297.0) // aka Hayford
internal val eWGS72 = Ellipsoid(6378135.0, 6356750.5, 1 / 298.26)

/**
 * Datums; with associated ellipsoid, and Helmert transform parameters to convert from WGS 84 into
 * given datum.
 *
 * Note that precision of various datums will vary, and WGS-84 (original) is not defined to be
 * accurate to better than ±1 metre. No transformation should be assumed to be accurate to better
 * than a meter; for many datums somewhat less.
 */
// transforms: t in metres, s in ppm, r in arcseconds                    tx       ty        tz       s        rx       ry       rz
val ED50 = Datum(eIntl1924, Transform(89.5, 93.8, 123.1, -1.2, 0.0, 0.0, 0.156))
val Irl1975 = Datum(eAiryModified, Transform(-482.530, 130.596, -564.557, -8.150, -1.042, -0.214, -0.631))
val NAD27 = Datum(eClarke1866, Transform(8.0, -160.0, -176.0, 0.0, 0.0, 0.0, 0.0))
val NAD83 = Datum(eGRS80, Transform(1.004, -1.910, -0.515, -0.0015, 0.0267, 0.00034, 0.011))
val NTF = Datum(eClarke1880IGN, Transform(168.0, 60.0, -320.0, 0.0, 0.0, 0.0, 0.0))
val OSGB36 = Datum(eAiry1830, Transform(-446.448, 125.157, -542.060, 20.4894, -0.1502, -0.2470, -0.8421))
val Potsdam = Datum(eBessel1841, Transform(-582.0, -105.0, -414.0, -8.3, 1.04, 0.35, -3.08))
val TokyoJapan = Datum(eBessel1841, Transform(148.0, -507.0, -685.0, 0.0, 0.0, 0.0, 0.0))
val WGS72 = Datum(eWGS72, Transform(0.0, 0.0, -4.5, -0.22, 0.0, 0.0, 0.554))
val WGS84 = Datum(eWGS84, Transform(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

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
