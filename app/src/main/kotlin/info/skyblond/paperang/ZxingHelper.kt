package info.skyblond.paperang


import com.google.zxing.common.BitMatrix
import info.skyblond.paperang.PaperangP2
import kotlin.experimental.or

fun BitMatrix.toByteArrays(): Array<ByteArray> {
    require(this.width <= PaperangP2.PRINT_BIT_PER_LINE) { "Too wide" }
    val widthLeftMargin = (PaperangP2.PRINT_BIT_PER_LINE - this.width) / 2
    val widthRightMargin = PaperangP2.PRINT_BIT_PER_LINE - this.width - widthLeftMargin
    return (0 until this.height).map { h ->
        val bits = (0 until this.width).map { this[it, h] }
        (BooleanArray(widthLeftMargin).toList() + bits + BooleanArray(widthRightMargin).toList())
            .chunked(8) {
                check(it.size == 8) { "Broken line" }
                var b: Byte = 0
                for (i in 0 until 8) {
                    if (it[i]) {
                        b = (b or (1 shl (7 - i)).toByte())
                    }
                }
                b
            }.toByteArray()
    }.toTypedArray()
}
