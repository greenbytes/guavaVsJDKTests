package org.greenbytes.guavaVsJDKTests.escape;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscaperTest {

    // seen in Oak org.apache.jackrabbit.oak.explorer.NodeStoreTree
    private String guavaEscaper(String s) {
        Escaper esc = Escapers.builder().setSafeRange(' ', '~')
                .addEscape('"', "\\\"").addEscape('\\', "\\\\").build();
        return esc.escape(s);
    }

    // replacements
    private String apacheEscaper(String s) {
        return org.apache.commons.text.StringEscapeUtils.escapeJava(s);
    }

    @Test
    void testEscaperGuava() {
        assertEquals("x", guavaEscaper("x"));
        assertEquals("\n", guavaEscaper("\n"));
        assertEquals("\u20ac", guavaEscaper("\u20ac"));
        assertEquals("\ud800", guavaEscaper("\ud800"));
        assertEquals("\\\"", guavaEscaper("\""));
        assertEquals("\\\\", guavaEscaper("\\"));
    }

    @Test
    void testEscaperCommons() {
        assertEquals("x", apacheEscaper("x"));
        assertEquals("\\n", apacheEscaper("\n"));
        assertEquals("\\u20AC", apacheEscaper("\u20ac"));
        assertEquals("\\uD800", apacheEscaper("\ud800"));
        assertEquals("\\\"", apacheEscaper("\""));
        assertEquals("\\\\", apacheEscaper("\\"));
    }
}
