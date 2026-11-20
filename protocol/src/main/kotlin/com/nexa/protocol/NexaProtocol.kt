package com.nexa.protocol

/** Constants shared by NEXA Mobile and NEXA TV. */
object NexaProtocol {
    const val VERSION = 1

    /** TCP port the TV listens on (falls back to an ephemeral port if busy; the QR carries the real one). */
    const val DEFAULT_PORT = 47800

    /** DNS-SD service type advertised by the TV so the phone can find it from the manual code. */
    const val NSD_SERVICE_TYPE = "_nexa._tcp."

    /** Hard cap for a single frame payload (a 1080p key frame is well below this). */
    const val MAX_FRAME_BYTES = 8 * 1024 * 1024
}

/** Every message on the wire: `[type: u8][length: i32 BE][payload]`. */
enum class MessageType(val code: Int) {
    HELLO(1),
    HELLO_OK(2),
    HELLO_REJECT(3),
    PING(4),
    PONG(5),

    SESSION_START(10),
    SESSION_END(11),
    SESSION_PAUSE(12),
    SESSION_RESUME(13),

    VIDEO_CONFIG(20),
    VIDEO_PACKET(21),
    REQUEST_KEYFRAME(22),

    AUDIO_CONFIG(30),
    AUDIO_PACKET(31),

    BYE(99);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCode(code: Int): MessageType? = byCode[code]
    }
}

class ProtocolException(message: String) : java.io.IOException(message)
