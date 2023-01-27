package info.skyblond.paperang

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class PaperangP2(
    btDevice: BluetoothDevice
) : AutoCloseable {
    companion object {
        const val PRINT_BYTE_PER_LINE = 72
        const val PRINT_BIT_PER_LINE = PRINT_BYTE_PER_LINE * Byte.SIZE_BITS

        private const val MAX_PACKET_SIZE_IN_BYTE = 1023

        const val LINE_PER_PACKET = MAX_PACKET_SIZE_IN_BYTE / PRINT_BYTE_PER_LINE

        private val RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private val COMMAND_PRINT_DATA = 0.toUByte()
        private val COMMAND_SET_HEAT_DENSITY = 25.toUByte()
        private val COMMAND_FEED_LINE = 26.toUByte()
        private val COMMAND_FEED_TO_HEAD_LINE = 33.toUByte()
        private val COMMAND_SET_PAPER_TYPE = 44.toUByte()
    }

    @SuppressLint("MissingPermission")
    private val btSocket = btDevice.createRfcommSocketToServiceRecord(RFCOMM_UUID)
    private val inputStream: InputStream
    private val outputStream: OutputStream

    init {
        btSocket.connect()
        inputStream = btSocket.inputStream
        outputStream = btSocket.outputStream
    }

    fun isConnected(): Boolean = btSocket.isConnected

    data class Packet(
        val command: UByte,
        val packetIndex: UByte,
        val data: ByteArray
    ) {
        companion object {
            fun read(inputStream: InputStream): Packet? {
                var r: Int
                do {
                    r = inputStream.read()
                } while (r != -1 && r != 0x02)
                if (r == -1) return null
                // now we get 0x02, packet start
                val command = inputStream.read().toUByte()
                val packetIndex = inputStream.read().toUByte()
                val dataLength =
                    bytesToShort(inputStream.read().toByte(), inputStream.read().toByte()).toInt()
                val data = ByteArray(dataLength)
                if (inputStream.read(data) != dataLength) return null // not enough bytes
                val crc = ByteArray(4)
                if (inputStream.read(crc) != 4) return null // not enough bytes
                if (inputStream.read() != 0x03) return null // expect packet end
                // check crc
                val calcCrc = CRC32().let {
                    it.reset(0x35769521L and 0xffffffffL)
                    it.update(data, 0, data.size)
                    it.value
                }
                return if (calcCrc.contentEquals(crc)) {
                    Packet(command, packetIndex, data)
                } else {
                    null // failed crc check
                }
            }
        }

        fun send(outputStream: OutputStream): Boolean {
            if (data.size >= Short.MAX_VALUE) return false // data to big
            outputStream.write(0x02) // packet start
            outputStream.write(command.toInt())
            outputStream.write(packetIndex.toInt())
            outputStream.write(data.size.toShort().toLittleEndian())
            outputStream.write(data)
            outputStream.write(CRC32().let {
                it.reset(0x35769521L and 0xffffffffL)
                it.update(data, 0, data.size)
                it.value
            })
            outputStream.write(0x03) // packet end
            outputStream.flush()
            return true
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Packet

            if (command != other.command) return false
            if (packetIndex != other.packetIndex) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = command.toInt()
            result = 31 * result + packetIndex.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    @Synchronized
    private fun sendPacket(packet: Packet, needReply: Boolean): List<Packet> {
        packet.send(outputStream)
        val result = mutableListOf<Packet>()
        if (needReply) {
            while (inputStream.available() != 0) {
                Packet.read(inputStream)?.let { result.add(it) } ?: break
            }
        }
        return result
    }

    /**
     * Send print data to printer.
     * @param lines array of lines, each element is a full line, size of [PRINT_BYTE_PER_LINE]
     * */
    fun sendPrintData(lines: Array<ByteArray>) {
        val remainLineCount = lines.size % LINE_PER_PACKET
        var packetCount = lines.size / LINE_PER_PACKET +
                if (remainLineCount == 0) 0 else 1

        require(packetCount <= UByte.MAX_VALUE.toInt())

        var linePointer = 0
        while (packetCount > 0) {
            packetCount--
            val lineCount = if (packetCount == 0 && remainLineCount != 0) {
                // last one is small packet
                remainLineCount
            } else {
                // send a full packet
                LINE_PER_PACKET
            }
            val buffer = ByteArray(lineCount * PRINT_BYTE_PER_LINE)
            var currentPacketPos = 0
            repeat(lineCount) {
                // for each line, copy to current packet
                System.arraycopy(
                    lines[linePointer++], 0,
                    buffer, currentPacketPos,
                    PRINT_BYTE_PER_LINE
                )
                currentPacketPos += PRINT_BYTE_PER_LINE
            }
            sendPacket(
                Packet(COMMAND_PRINT_DATA, packetCount.toUByte(), buffer), false
            )
        }
        check(packetCount == 0) { "Send data interrupted" }
    }


    /**
     * Set the heat density before print.
     * [density] max is 100, min is 0.
     * Note: 0 is not white, but every light.
     * */
    fun setHeatDensity(density: UByte) {
        sendPacket(
            Packet(COMMAND_SET_HEAT_DENSITY, 0u, byteArrayOf(density.toByte())),
            true
        )
    }


    /**
     * Feed some space, aka print nothing
     * */
    fun feedSpaceLine(amount: Short) {
        sendPacket(
            Packet(COMMAND_FEED_LINE, 0u, amount.toLittleEndian()), true
        )
    }


    /**
     * Same as [feedSpaceLine], not sure why.
     * */
    fun feedToHeadLine(amount: Short) {
        sendPacket(
            Packet(COMMAND_FEED_TO_HEAD_LINE, 0u, amount.toLittleEndian()),
            true
        )
    }


    /**
     * Set the paper type.
     * 0x00 is normal paper.
     * Other value is unknown.
     * */
    fun setPaperType(paperType: UByte = 0x00u) {
        sendPacket(
            Packet(COMMAND_SET_PAPER_TYPE, 0u, byteArrayOf(paperType.toByte())), true
        )
    }

    override fun close() {
        btSocket.close()
    }

}