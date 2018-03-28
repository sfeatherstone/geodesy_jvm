import com.sfeatherstone.geodesy.LatLon
import com.sfeatherstone.geodesy.grids.Hemisphere
import com.sfeatherstone.geodesy.grids.Utm
import com.sfeatherstone.geodesy.grids.toLatLonE
import com.sfeatherstone.geodesy.grids.toUtm
import com.sfeatherstone.geodesy.toFixed

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */
/*  MGRS / UTM Conversion Functions                                   (c) Chris Veness 2014-2016  */
/*                                                                        Simon Featherstone 2018 */
/*                                                                                   MIT Licence  */
/* www.movable-type.co.uk/scripts/latlong-utm-mgrs.html                                           */
/* www.movable-type.co.uk/scripts/geodesy/docs/module-mgrs.html                                   */
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

/**
 * Convert between Universal Transverse Mercator (UTM) coordinates and Military Grid Reference
 * System (MGRS/NATO) grid references.
 */


/**
 * Creates an Mgrs grid reference object.
 *
 * @constructor
 * @param  {number} zone - 6° longitudinal zone (1..60 covering 180°W..180°E).
 * @param  {string} band - 8° latitudinal band (C..X covering 80°S..84°N).
 * @param  {string} e100k - First letter (E) of 100km grid square.
 * @param  {string} n100k - Second letter (N) of 100km grid square.
 * @param  {number} easting - Easting in metres within 100km grid square.
 * @param  {number} northing - Northing in metres within 100km grid square.
 * @param  {LatLon.datum} [datum=WGS84] - Datum UTM coordinate is based on.
 * @throws {Error}  Invalid MGRS grid reference.
 *
 * @example
 *   var mgrsRef = new Mgrs(31, 'U', 'D', 'Q', 48251, 11932); // 31U DQ 48251 11932
 */

data class Mgrs(val zone: Int, val band: Char, val e100k: Char, val n100k: Char, val easting: Double, val northing: Double) {
/**
 * Returns a string representation of an MGRS grid reference.
 *
 * To distinguish from civilian UTM coordinate representations, no space is included within the
 * zone/band grid zone designator.
 *
 * Components are separated by spaces: for a military-style unseparated string, use
 * Mgrs.toString().replace(/ /g, '');
 *
 * Note that MGRS grid references get truncated, not rounded (unlike UTM coordinates).
 *
 * @param   {number} [digits=10] - Precision of returned grid reference (eg 4 = km, 10 = m).
 * @returns {string} This grid reference in standard format.
 * @throws  {Error}  Invalid precision.
 *
 * @example
 *   var mgrsStr = new Mgrs(31, 'U', 'D', 'Q', 48251, 11932).toString(); // '31U DQ 48251 11932'
 */

    fun toString(digits: Int) :String{
    //    digits = (digits === undefined) ? 10 : Number(digits);
        if (arrayOf( 2,4,6,8,10 ).indexOf(digits) == -1) throw Exception("Invalid precision ‘"+digits.toString()+"’")

    // truncate to required precision
        val truncationValue = Math.pow(10.0, (5-digits/2).toDouble())
        var eRounded = Math.floor(this.easting/truncationValue).toInt()
        var nRounded = Math.floor(this.northing/truncationValue).toInt()


        val subFormat = "%0${digits/2}d"

        return "%02d%c %c%c $subFormat $subFormat".format(zone, band, e100k, n100k, eRounded, nRounded)
        }

    override fun toString() = toString(10)

    companion object {
         /*
         * Latitude bands C..X 8° each, covering 80°S to 84°N
         */
        const val latBands = "CDEFGHJKLMNPQRSTUVWXX" // X is repeated for 80-84°N


        /*
         * 100km grid square column (‘e’) letters repeat every third zone
         */
        val e100kLetters = arrayOf("ABCDEFGH", "JKLMNPQR", "STUVWXYZ")


        /*
         * 100km grid square row (‘n’) letters repeat every other zone
         */
        val n100kLetters = arrayOf("ABCDEFGHJKLMNPQRSTUV", "FGHJKLMNPQRSTUVABCDE")

    }
}

/**
 * Converts UTM coordinate to MGRS reference.
 *
 * @returns {Mgrs}
 * @throws  {Error} Invalid UTM coordinate.
 *
 * @example
 *   var utmCoord = new Utm(31, 'N', 448251, 5411932);
 *   var mgrsRef = utmCoord.toMgrs(); // 31U DQ 48251 11932
 */
fun Utm.toMgrs(): Mgrs {
    if (this.easting.isNaN() || this.northing.isNaN()) throw Exception("Invalid UTM coordinate ‘"+this.toString()+"’")

    // MGRS zone is same as UTM zone
    var zone = this.zone

    // convert UTM to lat/long to get latitude to determine band
    var latlong = this.toLatLonE()
    // grid zones are 8° tall, 0°N is 10th band
    var band = Mgrs.latBands[Math.floor(latlong.lat/8+10).toInt()] // latitude band

    // columns in zone 1 are A-H, zone 2 J-R, zone 3 S-Z, then repeating every 3rd zone
    var col = Math.floor(this.easting / 100e3).toInt()
    var e100k = Mgrs.e100kLetters[(zone-1)%3][col-1] // col-1 since 1*100e3 -> A (index 0), 2*100e3 -> B (index 1), etc.

    // rows in even zones are A-V, in odd zones are F-E
    var row = Math.floor(this.northing / 100e3).toInt() % 20
    var n100k = Mgrs.n100kLetters[(zone-1)%2][row]

    // truncate easting/northing to within 100km grid square
    var easting = this.easting % 100e3
    var northing = this.northing % 100e3

    // round to nm precision
    easting = easting.toFixed(6)
    northing = northing.toFixed(6)

    return Mgrs(zone, band, e100k, n100k, easting, northing)
}


/**
 * Converts MGRS grid reference to UTM coordinate.
 *
 * @returns
 *
 * @example
 *   var utmCoord = Mgrs.parse('31U DQ 448251 11932').toUtm(); // 31 N 448251 5411932
 */
fun Mgrs.toUtm(): Utm {
    var zone = this.zone
    var band = this.band
    var e100k = this.e100k
    var n100k = this.n100k
    var easting = this.easting
    var northing = this.northing

    var hemisphere = if (band>='N') Hemisphere.NORTH else Hemisphere.SOUTH

    // get easting specified by e100k
    var col = Mgrs.e100kLetters[(zone-1)%3].indexOf(e100k) + 1 // index+1 since A (index 0) -> 1*100e3, B (index 1) -> 2*100e3, etc.
    var e100kNum = col * 100e3 // e100k in metres

    // get northing specified by n100k
    var row = Mgrs.n100kLetters[(zone-1)%2].indexOf(n100k)
    var n100kNum = row * 100e3 // n100k in metres

    // get latitude of (bottom of) band
    var latBand = (Mgrs.latBands.indexOf(band)-10.0)*8.0

    // northing of bottom of band, extended to include entirety of bottommost 100km square
    // (100km square boundaries are aligned with 100km UTM northing intervals)
    var nBand = Math.floor(LatLon(latBand, 0.0).toUtm().northing/100e3)*100e3
    // 100km grid square row letters repeat every 2,000km north; add enough 2,000km blocks to get
    // into required band
    var n2M = 0.0 // northing of 2,000km block
    while (n2M + n100kNum + northing < nBand) n2M += 2000e3

    return Utm(zone, hemisphere, e100kNum + easting, n2M + n100kNum + northing)
}


/**
 * Parses string representation of MGRS grid reference.
 *
 * An MGRS grid reference comprises (space-separated)
 *  - grid zone designator (GZD)
 *  - 100km grid square letter-pair
 *  - easting
 *  - northing.
 *
 * @param   {string} mgrsGridRef - String representation of MGRS grid reference.
 * @returns {Mgrs}   Mgrs grid reference object.
 * @throws  {Error}  Invalid MGRS grid reference.
 *
 * @example
 *   var mgrsRef = Mgrs.parse('31U DQ 48251 11932');
 *   var mgrsRef = Mgrs.parse('31UDQ4825111932');
 *   //  mgrsRef: { zone:31, band:'U', e100k:'D', n100k:'Q', easting:48251, northing:11932 }
 */
fun String.parseMgsr(): Mgrs?{

    try {
        var str = this.trim().toUpperCase()

        // check for military-style grid reference with no separators
        if (!Regex("\\s").containsMatchIn(str)) {
            val en = str.substring(5) // get easting/northing following zone/band/100ksq
            val newStr = str.substring(0..2) + " " + str.substring(3..4) + " " + en.substring(0..(en.length / 2)-1) + " " + en.substring(en.length / 2) // separate easting/northing

            //en = en.slice(0, en.length/2)+' '+en.slice(-en.length/2); // separate easting/northing
            //mgrsGridRef = mgrsGridRef.slice(0, 3)+' '+mgrsGridRef.slice(3, 5)+' '+en; // insert spaces
            str = newStr
        }

        // match separate elements (separated by whitespace)
        val mgrsGridRef = Regex("""\S+""").findAll(str).toList()

        if (mgrsGridRef.size != 4) throw Exception("Invalid UTM coordinate ‘" + this + "’")

        //mgrsGridRef = mgrsGridRef.match("""\S+/g""");

        if (mgrsGridRef.size != 4) throw Exception("Invalid MGRS grid reference ‘" + mgrsGridRef + "’")

        // split gzd into zone/band
        var gzd = mgrsGridRef[0].value
        var zone = gzd.substring(0..1).toInt()
        var band = gzd[2]

        // split 100km letter-pair into e/n
        var en100k = mgrsGridRef[1].value
        var e100k = en100k[0]
        var n100k = en100k[1]

        fun trimLeadingZerosAndStandardiseTo10digit(input:String): Double {
            val trimmed = input.trim()
            val standardised = if (trimmed.length>=5) trimmed else (trimmed + "00000").substring(0..4)
            val zeroTrimmed = standardised.trimStart('0')
            return if (zeroTrimmed.length == 0) standardised.substring(0..1).toDouble() else zeroTrimmed.toDouble()
        }

        return Mgrs(zone, band, e100k, n100k,
                trimLeadingZerosAndStandardiseTo10digit(mgrsGridRef[2].value),
                trimLeadingZerosAndStandardiseTo10digit(mgrsGridRef[3].value))
    } catch (e: Exception) {
        return null
    }
}



