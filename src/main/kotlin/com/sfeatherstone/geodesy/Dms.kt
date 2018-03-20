package com.sfeatherstone.geodesy
import kotlin.math.roundToInt

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/* Geodesy representation conversion functions                        (c) Chris Veness 2002-2017  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong.html                                                    */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-dms.html                                    */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


/**
 * Latitude/longitude points may be represented as decimal degrees, or subdivided into sexagesimal
 * minutes and seconds.
 *
 * @module dms
 */


/**
 * Functions for parsing and representing degrees / minutes / seconds.
 * @class Dms
 */

// note Unicode Degree = U+00B0. Prime = U+2032, Double prime = U+2033

    /**
     * Parses string representing degrees/minutes/seconds into numeric degrees.
     *
     * This is very flexible on formats, allowing signed decimal degrees, or deg-min-sec optionally
     * suffixed by compass direction (NSEW). A variety of separators are accepted (eg 3° 37′ 09″W).
     * Seconds and minutes may be omitted.
     *
     * @param   {string|number} dmsStr - Degrees or deg/min/sec in variety of formats.
     * @returns {number} Degrees as decimal number.
     *
     * @example
     *     var lat = Dms.parseDMS('51° 28′ 40.12″ N');
     *     var lon = Dms.parseDMS('000° 00′ 05.31″ W');
     *     var p1 = new LatLon(lat, lon); // 51.4778°N, 000.0015°W
     */
    fun String.parseDegreesMinutesSeconds(): Double {
        // strip off any sign or compass dir'n & split out separate d/m/s
        var dms = this.trim().replace(Regex("^-"), "")
                .replace(Regex("[NSEW]$/i") , "")
                .split(Regex("[^0-9.]+"))

        if (dms.size > 1 && dms[dms.lastIndex].isNullOrBlank()) dms = dms.subList(0,dms.lastIndex)  // from trailing symbol

        if (dms[0] == "") return Double.NaN

        // and convert to decimal degrees...
        val deg : Double
        when(dms.size) {
            3 ->  // interpret 3-part result as d/m/s
                deg = (dms[0].toDouble() / 1.0) + (dms[1].toDouble() / 60.0) + (dms[2].toDouble() / 3600)
            2 ->  // interpret 2-part result as d/m
                deg = (dms[0].toDouble() / 1.0) + (dms[1].toDouble() / 60.0)
            1 ->  // just d (possibly decimal) or non-separated dddmmss
                deg = dms[0].toDouble()
            // check for fixed-width unseparated format eg 0033709W
            //if (/[NS]/i.test(dmsStr)) deg = '0' + deg;  // - normalise N/S to 3-digit degrees
            //if (/[0-9]{7}/.test(deg)) deg = deg.slice(0,3)/1 + deg.slice(3,5)/60 + deg.slice(5)/3600;
            else ->
                return Double.NaN
        }
        return if ( Regex("^-|[WSws]$").containsMatchIn(this.trim())) -deg else deg // take '-', west and south as -ve
    }


/**
     * Separator character to be used to separate degrees, minutes, seconds, and cardinal directions.
     *
     * Set to '\u202f' (narrow no-break space) for improved formatting.
     *
     * @example
     *   var p = new LatLon(51.2, 0.33);  // 51°12′00.0″N, 000°19′48.0″E
     *   Dms.separator = '\u202f';        // narrow no-break space
     *   var pʹ = new LatLon(51.2, 0.33); // 51° 12′ 00.0″ N, 000° 19′ 48.0″ E
     */
    val narrowNoBreakSeparator = "\u202f"


    /**
     * Converts decimal degrees to deg/min/sec format
     *  - degree, prime, double-prime symbols are added, but sign is discarded, though no compass
     *    direction is added.
     *
     * @private
     * @param   {number} deg - Degrees to be formatted as specified.
     * @param   {string} [format=dms] - Return value as 'd', 'dm', 'dms' for deg, deg+min, deg+min+sec.
     * @param   {number} [dp=0|2|4] - Number of decimal places to use – default 0 for dms, 2 for dm, 4 for d.
     * @returns {string} Degrees formatted as deg/min/secs according to specified format.
     */
    fun Double.toDMS(format: String = "dms", dp: Int? = null, separator : String = ""): String?
    {
        if (this == Double.NaN) return null  // give up here if we can't make a number from deg

        val _dp = if (dp == null) {
            when(format) {
                "d", "deg" -> 4
                "dm","deg+min" -> 2
                "dms", "deg+min+sec" -> 0
                else -> 0
            }
        } else dp

        val degrees : Double = Math.abs(this % 360.0)  // (unsigned result ready for appending compass dir'n)

        return when (format) {
            "dm","deg+min" -> toDegreesMinutes(degrees, _dp, separator)
            "dms", "deg+min+sec" -> toDegreesMinutesSeconds(degrees, _dp, separator)
            "d", "deg" -> toDegrees(degrees, _dp)
            else -> toDegreesMinutesSeconds(degrees, _dp, separator)
        }
    }

    internal fun toDegrees(degrees: Double, dp: Int) : String {
        val formatStr = if (dp==0) "%03.0f°" else "%0${dp+4}.0${dp}f°"
        return formatStr.format(degrees)
    }

    internal fun toDegreesMinutes(degrees: Double, dp: Int, separator : String) : String {
        var d = Math.floor(degrees).toInt()                // get component deg
        var m = ((degrees * 60.0) % 60.0).toFixed(dp)           // get component min & round/right-pad
        if (m == 60.0) {
            m = 0.0
            d++
        }               // check for rounding up
        return "%03d°%s%02.${dp}f′".format(d, separator, m)
    }

    internal fun toDegreesMinutesSeconds(degrees: Double, dp: Int, separator: String) : String {
        var d = Math.floor(degrees).toInt()                       // get component deg
        var m = (Math.floor((degrees * 3600.0) / 60.0) % 60.0).toInt()  // get component min
        var s = (degrees * 3600.0 % 60.0).toFixed(dp)           // get component sec & round/right-pad
        if (s == 60.0) {
            s = 0.0
            m++
        } // check for rounding up

        if (m >= 60) {
            m -= 60
            d++
        }               // check for rounding up

        val minsLength = if (dp==0) 2 else dp + 3

/*                d = ('000' + d).slice(-3);                   // left-pad with leading zeros
                m = ('00' + m).slice(-2);                    // left-pad with leading zeros
                if (s < 10) s = '0' + s;                     // left-pad with leading zeros (note may include decimals)
                dms = d + '°' + Dms.separator + m + '′' + Dms.separator + s + '″'*/
        return "%03d°%s%02d′%s%0$minsLength.${dp}f″".format(d, separator, m, separator, s)
    }


    /**
     * Converts numeric degrees to deg/min/sec latitude (2-digit degrees, suffixed with N/S).
     *
     * @param   {number} deg - Degrees to be formatted as specified.
     * @param   {string} [format=dms] - Return value as 'd', 'dm', 'dms' for deg, deg+min, deg+min+sec.
     * @param   {number} [dp=0|2|4] - Number of decimal places to use – default 0 for dms, 2 for dm, 4 for d.
     * @returns {string} Degrees formatted as deg/min/secs according to specified format.
     */
    fun Double.toLatitude(format: String, dp: Int? = null, separator: String = ""): String
    {
        val lat = this.toDMS(format, dp, separator)
        return if (lat == null) "–" else lat.substring(1)+separator+(if (this<0.0) 'S' else 'N')  // knock off initial '0' for lat!
    }


/**
     * Convert numeric degrees to deg/min/sec longitude (3-digit degrees, suffixed with E/W)
     *
     * @param   {number} deg - Degrees to be formatted as specified.
     * @param   {string} [format=dms] - Return value as 'd', 'dm', 'dms' for deg, deg+min, deg+min+sec.
     * @param   {number} [dp=0|2|4] - Number of decimal places to use – default 0 for dms, 2 for dm, 4 for d.
     * @returns {string} Degrees formatted as deg/min/secs according to specified format.
     */
    fun Double.toLongitude(format: String, dp: Int? = null, separator : String = ""): String
    {
        val lon = this.toDMS(format, dp, separator)
        return if (lon == null) "–" else lon+separator+(if (this<0.0) 'W' else 'E')
    }


/**
     * Converts numeric degrees to deg/min/sec as a bearing (0°..360°)
     *
     * @param   {number} deg - Degrees to be formatted as specified.
     * @param   {string} [format=dms] - Return value as 'd', 'dm', 'dms' for deg, deg+min, deg+min+sec.
     * @param   {number} [dp=0|2|4] - Number of decimal places to use – default 0 for dms, 2 for dm, 4 for d.
     * @returns {string} Degrees formatted as deg/min/secs according to specified format.
     */
    fun Double.toBearing(format: String = "dms", dp: Int = 0, separator : String = ""): String
    {
        val degNormalised = (this + 360) % 360  // normalise -ve values to 180°..360°
        val brng = degNormalised.toDMS(format, dp, separator)
        return if (brng == null) "–" else brng.replace("360", "0")  // just in case rounding took us up to 360°!
    }


/**
     * Returns compass point (to given precision) for supplied bearing.
     *
     * @param   bearing - Bearing in degrees from north.
     * @param   precision=3 - Precision (1:cardinal / 2:intercardinal / 3:secondary-intercardinal).
     * @returns Compass point for supplied bearing.
     *
     * @example
     *   var point = Dms.compassPoint(24.0);    // point = 'NNE'
     *   var point = Dms.compassPoint(24.0, 1); // point = 'N'
     */
    fun compassPoint(bearing : Double, precision: Int = 3): String
    {
        // note precision could be extended to 4 for quarter-winds (eg NbNW), but I think they are little used

        val normalisedBearing = ((bearing % 360) + 360) % 360 // normalise to range 0..360°

        val cardinals = arrayOf(
            "N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW")
        val n = when(precision) {
            1 -> 4
            2 -> 8
            3-> 16
            else -> 16
        }
        return cardinals[(normalisedBearing * n / 360.0).roundToInt() % n * 16 / n]
    }



