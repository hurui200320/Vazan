package info.skyblond.vazan.domain

import kotlin.math.pow
import kotlin.random.Random

object LabelEncoding {

    const val charset = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    enum class LabelType(
        val prefix: String,
        val digits: List<String>
    ) {
        BOX(
            "B", listOf(
                "P4RQZ8MYLCBSKXWUJ35F6VDG79AN2EHT",
                "TYZK2CG9AWD7RNSPQHEJX36M4FLB58UV",
                "V2DHKNAQFJ3CR5BGZ76PMT4L9UXWESY8",
                "8ZY4NSBEVT95PQAL2FUDKG3XR6WJ7CMH",
                "6BXS7WF5UDHACV29KQYMNLTJG3EZ4RP8",
                "3D62CPXR4BGH9V78N5ASJUMLKFWYEQZT",
                "RC964AHG3WZFT2L8BMXQDSUVJYEP75NK",
                "HG8UMXZVQTF6PRBKSJDWYA972CL5N43E",
                "DH5EU4WPGJCFZ39NSK2XYTAVLBMR86Q7",
            )
        ),
        ITEM(
            "I", listOf(
                "CQ7LVX84SNYH3ZFREBK5GDAPM6T92WUJ",
                "WUGQ4E6YMT39CFV7ZLP2AS5BKXRNDJ8H",
                "GE4T6FDMA3CNZ285W7B9YUXPKSRLJQVH",
                "F3PRX7EN5TJW4AMSCDG9BKU82VZLQ6HY",
                "V8WHLAJ9P6DGQYB3CNS5XTF472MERUZK",
                "3ZEPMVAWKGLNB9Y754JQCDRUF2SH86XT",
                "UVAW5PS8JFGXMDQ9NHKZB76LTEYC24R3",
                "PHRYF2KBLAQCVSD7XJMTZ8W4N5GU9E63",
                "RBKUJ47DYWXHE6MC9SAP5FV3TG28ZQLN",
            )
        )
    }

    /**
     * The fixed length of the encoded string.
     * */
    private const val length = 9

    /**
     * How many chars are available for one digit.
     * */
    private const val charCount = 32

    /**
     * The minimal value this encoding can encode.
     * */
    const val minValue = 0L

    /**
     * The maximal value this encoding can encode.
     *
     * value: 32^9 - 1
     * */
    const val maxValue = 35184372088831

    fun random(r: Random = Random) = r.nextLong(minValue, maxValue + 1)

    private fun isInRange(value: Long): Boolean = value in minValue..maxValue
    private fun isInRange(label: String): Boolean = label.length <= length

    fun encodeToLabel(type: LabelType, value: Long): String {
        require(isInRange(value)) { "Value out of range: $value. Min: $minValue, max: $maxValue" }
        var t = value
        return type.prefix + type.digits.indices.map {
            type.digits[it][(t % charCount).toInt()].also { t /= charCount }
        }.joinToString("")
    }

    fun decodeFromLabel(label: String): Long {
        val type = when (label[0].toString()) {
            LabelType.BOX.prefix -> LabelType.BOX
            LabelType.ITEM.prefix -> LabelType.ITEM
            else -> error("Unknown label type: $label")
        }
        val content = label.drop(1)
        require(isInRange(content)) { "Label length is too long, max: $length, got: ${content.length}" }
        return content.foldIndexed(0L) { index, acc, c ->
            acc + type.digits[index].indexOf(c) * (32.0.pow(index)).toLong()
        }
    }
}