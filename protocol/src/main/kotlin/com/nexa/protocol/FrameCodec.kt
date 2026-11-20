package com.nexa.protocol

import java.io.DataInputStream
import java.io.DataOutputStream

class Frame(val type: MessageType, val payload: ByteArray)

/** Length-prefixed framing. Callers must serialize writes and flush when appropriate. */
object FrameCodec {

    fun write(out: DataOutputStream, type: MessageType, payload: ByteArray = EMPTY) {
        require(payload.size <= NexaProtocol.MAX_FRAME_BYTES) { "Frame too large: ${payload.size}" }
        out.writeByte(type.code)
        out.writeInt(payload.size)
        out.write(payload)
    }

    /** Reads the next known frame. Frames with unknown types are skipped (forward compatibility). */
    fun read(input: DataInputStream): Frame {
        while (true) {
            val code = input.readUnsignedByte()
            val length = input.readInt()
            if (length < 0 || length > NexaProtocol.MAX_FRAME_BYTES) {
                throw ProtocolException("Invalid frame length $length")
            }
            val payload = ByteArray(length)
            input.readFully(payload)
            val type = MessageType.fromCode(code) ?: continue
            return Frame(type, payload)
        }
    }

    val EMPTY = ByteArray(0)
}
