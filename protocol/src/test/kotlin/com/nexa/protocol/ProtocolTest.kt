package com.nexa.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class ProtocolTest {

    @Test
    fun frameRoundTripSkipsUnknownTypes() {
        val bytes = ByteArrayOutputStream()
        val out = DataOutputStream(bytes)
        out.writeByte(200); out.writeInt(3); out.write(byteArrayOf(1, 2, 3)) // unknown type
        FrameCodec.write(out, MessageType.HELLO, Hello(1, "Pixel", token = "abc").encode())
        val frame = FrameCodec.read(DataInputStream(ByteArrayInputStream(bytes.toByteArray())))
        assertEquals(MessageType.HELLO, frame.type)
        assertEquals(Hello(1, "Pixel", token = "abc"), Hello.decode(frame.payload))
    }

    @Test
    fun keyValueEscapesSeparators() {
        val m = mapOf("a" to "x=y\nz&w", "b" to "ç")
        assertEquals(m, KeyValue.decode(KeyValue.encode(m)))
    }

    @Test
    fun videoPacketRoundTrip() {
        val data = byteArrayOf(0, 0, 0, 1, 0x65)
        val p = VideoPacket.decode(VideoPacket.encode(VideoPacket.FLAG_KEY_FRAME, 123_456L, data))
        assertTrue(p.isKeyFrame)
        assertEquals(123_456L, p.ptsUs)
        assertArrayEquals(data, p.data)
    }

    @Test
    fun pairingCodeNormalization() {
        assertEquals("NX-8K4Q", PairingCode.normalize("nx-8k4q"))
        assertEquals("NX-8K4Q", PairingCode.normalize("8k4q"))
        assertEquals("NX-8K4Q", PairingCode.normalize(" NX 8K4Q "))
        assertNull(PairingCode.normalize("NX-80"))
        assertNull(PairingCode.normalize("NX-0OI1")) // ambiguous chars not in alphabet
    }

    @Test
    fun gateAcceptsTokenOrCodeAndRegeneratesAfterFailures() {
        var now = 0L
        val gate = PairingGate(clock = { now }, maxFailures = 3)
        val s = gate.current
        assertEquals(PairingVerdict.ACCEPTED, gate.verify(s.token, null))
        assertEquals(PairingVerdict.ACCEPTED, gate.verify(null, s.code.lowercase()))
        assertEquals(PairingVerdict.INVALID, gate.verify("nope", null))
        assertEquals(PairingVerdict.INVALID, gate.verify(null, "NX-AAAA".takeIf { it != s.code } ?: "NX-BBBB"))
        assertEquals(PairingVerdict.TOO_MANY_ATTEMPTS, gate.verify(null, null))
        assertNotEquals(s.token, gate.current.token)
        assertEquals(PairingVerdict.INVALID, gate.verify(s.token, null)) // old token no longer valid
    }

    @Test
    fun gateExpiresSessions() {
        var now = 0L
        val gate = PairingGate(clock = { now })
        val s = gate.current
        now += PairingSession.DEFAULT_TTL_MS
        assertEquals(PairingVerdict.EXPIRED, gate.verify(s.token, null))
        assertNotEquals(s.token, gate.current.token)
    }

    @Test
    fun pairingUriRoundTrip() {
        val info = PairingInfo("192.168.1.42", 47800, "AbCdEfGhIjKlMnOpQrStUv", "TCL Sala & TV")
        assertEquals(info, PairingUri.parse(PairingUri.build(info)))
        assertNull(PairingUri.parse("https://example.com"))
        assertNull(PairingUri.parse("nexa://pair?h=1.2.3.4&p=0&t=AbCdEfGhIjKlMnOpQrStUv"))
    }
}
