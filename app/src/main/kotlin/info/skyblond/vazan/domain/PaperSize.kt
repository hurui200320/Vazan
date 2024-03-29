package info.skyblond.vazan.domain

import android.graphics.Bitmap
import info.skyblond.vazan.domain.PrintUtils.collapse
import info.skyblond.vazan.domain.PrintUtils.toDataArray


enum class PaperSize(
    val displayName: String,
    val gap: Int,
    val generatePreview: (String) -> Bitmap,
    /**
     * Turn the label and repeat time into printable instructions.
     * */
    val generatePrintData: (String, Int) -> ByteArray
) {
    PAPER_80_60_2(
        displayName = "80mm x 60mm", gap = 2,
        generatePreview = { PrintUtils.generate80By60(it) },
        generatePrintData = { str, repeat ->
            val imageData = PrintUtils.generate80By60(str).toDataArray()

            "SIZE 80 mm,60 mm\r\n".encodeToByteArray() +
                    "GAP 2 mm,0 mm\r\n".encodeToByteArray() +
                    "DENSITY 2\r\n".encodeToByteArray() +
                    "SPEED 1.5\r\n".encodeToByteArray() +
                    "CLS\r\n".encodeToByteArray() +
                    "BITMAP 0,0,${imageData[0].size / 8},${imageData.size},0,".encodeToByteArray() +
                    imageData.map { row -> row.map { it == 0 }.toBooleanArray() }
                        .toTypedArray().collapse() + "\r\n".encodeToByteArray() +
                    "PRINT ${repeat}\r\n".encodeToByteArray()
        }
    ),
    PAPER_60_80_2(displayName = "60mm x 80mm", gap = 2,
        generatePreview = { PrintUtils.generate60By80(it) },
        generatePrintData = { str, repeat ->
            val imageData = PrintUtils.generate60By80(str).toDataArray()

            "SIZE 60 mm,80 mm\r\n".encodeToByteArray() +
                    "GAP 2 mm,0 mm\r\n".encodeToByteArray() +
                    "DENSITY 2\r\n".encodeToByteArray() +
                    "SPEED 1.5\r\n".encodeToByteArray() +
                    "CLS\r\n".encodeToByteArray() +
                    "BITMAP 0,0,${imageData[0].size / 8},${imageData.size},0,".encodeToByteArray() +
                    imageData.map { row -> row.map { it == 0 }.toBooleanArray() }
                        .toTypedArray().collapse() + "\r\n".encodeToByteArray() +
                    "PRINT ${repeat}\r\n".encodeToByteArray()
        }),
    PAPER_70_30_2(displayName = "70mm x 30mm", gap = 2,
        generatePreview = { PrintUtils.generate70By30(it) },
        generatePrintData = { str, repeat ->
            val imageData = PrintUtils.generate70By30(str).toDataArray()

            "SIZE 70 mm,30 mm\r\n".encodeToByteArray() +
                    "GAP 2 mm,0 mm\r\n".encodeToByteArray() +
                    "DENSITY 2\r\n".encodeToByteArray() +
                    "SPEED 1.5\r\n".encodeToByteArray() +
                    "CLS\r\n".encodeToByteArray() +
                    "BITMAP 0,0,${imageData[0].size / 8},${imageData.size},0,".encodeToByteArray() +
                    imageData.map { row -> row.map { it == 0 }.toBooleanArray() }
                        .toTypedArray().collapse() + "\r\n".encodeToByteArray() +
                    "PRINT ${repeat}\r\n".encodeToByteArray()
        }),
}