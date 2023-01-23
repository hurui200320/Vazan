package info.skyblond.vazan.scanner

import android.content.Intent
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ResultCounter {
    private val resultCounter = AtomicInteger(0)
    private val resultMap = ConcurrentHashMap<Int, ConcurrentHashMap<ByteString, Int>>()

    private data class ByteString(val data: ByteArray){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteString

            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }

    }

    @Synchronized
    fun countResult(barcodes: List<Barcode?>?): Int {
        if (barcodes == null) return resultCounter.get()
        val list = barcodes.filter {
            it != null
                    && it.format != Barcode.FORMAT_UNKNOWN
                    && it.rawBytes != null
        }
        if (list.isEmpty()) return resultCounter.get()
        val counter = resultCounter.incrementAndGet()
        list.forEach { barcode ->
            resultMap
                .getOrPut(barcode!!.format) { ConcurrentHashMap<ByteString, Int>() }
                .compute(ByteString(barcode.rawBytes!!)) { _, v -> v?.let { v + 1 } ?: 1 }
        }
        return counter
    }

    @Synchronized
    fun getResult(minCount: Int): Intent {
        val intent = Intent()
        var size = 0L
        resultMap.forEach { (format, map) ->
            map.forEach { (value, count) ->
                if (count >= minCount) {
                    intent.putExtra("format$size", format)
                    intent.putExtra("data$size", value.data)
                    size++
                }
            }
        }
        intent.putExtra("size", size)
        resultMap.clear()
        return intent
    }
}