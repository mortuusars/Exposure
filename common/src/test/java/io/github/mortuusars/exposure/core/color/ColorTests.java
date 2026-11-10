package io.github.mortuusars.exposure.core.color;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.mortaar.util.color.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorTests {
    @Test
    void ARGBtoABGR() {
        assertEquals(0x99AA3322, Color.argb(0x992233AA).getABGR());
        assertEquals(0x99AA3322, Color.ARGBtoABGR(0x992233AA));
    }

    @Test
    void floats() {
        Color color = Color.rgb(0xFFFFFF).multiply(0.5);
        assertEquals(0.498f, color.getRF(), 0.01f);
        assertEquals(0.498f, color.getGF(), 0.01f);
        assertEquals(0.498f, color.getBF(), 0.01f);
    }

    @Test
    void hexParsesCorrectly() {
        Color white = Color.fromHex("FFFFFFFF");
        assertEquals(255, white.getA());
        assertEquals(255, white.getR());
        assertEquals(255, white.getG());
        assertEquals(255, white.getB());

        Color black = Color.fromHex("00000000");
        assertEquals(0, black.getA());
        assertEquals(0, black.getR());
        assertEquals(0, black.getG());
        assertEquals(0, black.getB());

        Color random = Color.fromHex("7FC2C351");
        assertEquals(127, random.getA());
        assertEquals(194, random.getR());
        assertEquals(195, random.getG());
        assertEquals(81, random.getB());

        Color noAlpha = Color.fromHex("C2C351");
        assertEquals(0, noAlpha.getA());
        assertEquals(194, noAlpha.getR());
        assertEquals(195, noAlpha.getG());
        assertEquals(81, noAlpha.getB());
    }

    @Test
    void intCodec() {
        int integer = Color.CODEC.encodeStart(JsonOps.INSTANCE, Color.fromHex("7F7F7F7F")).getOrThrow().getAsInt();
        assertEquals(0x7F7F7F7F, integer);

        Color decodedColor = Color.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(0x7F7F7F7F)).getOrThrow().getFirst();
        assertEquals(0x7F7F7F7F, decodedColor.getARGB());
    }

    @Test
    void hexCodec() {
        String json = Color.HEX_STRING_CODEC.encodeStart(JsonOps.INSTANCE, Color.argb(0x7F7F7F7F)).getOrThrow().getAsString();
        assertEquals("7F7F7F7F", json);

        Color decodedColor = Color.HEX_STRING_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive("7F7F7F7F")).getOrThrow().getFirst();
        assertEquals(0x7F7F7F7F, decodedColor.getARGB());
    }
}
