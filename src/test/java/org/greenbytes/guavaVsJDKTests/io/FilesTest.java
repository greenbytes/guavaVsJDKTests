package org.greenbytes.guavaVsJDKTests.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilesTest {

    @TempDir
    File tmp;

    Path txtOk, txtBroken;
    String sTxtOk = "\uD834\uDD1E";
    byte[] bTxtOk = new byte[] {(byte)0xF0, (byte)0x9D, (byte)0x84, (byte)0x9E};
    byte[] bTxtBroken = new byte[] {(byte)0xd8, (byte)0x00};
    String sTxtBrokenWithReplacement = "\ufffd\u0000";

    @BeforeEach
    public void setup() throws IOException {
        txtOk = Files.createTempFile(tmp.toPath(), "txtOk", null);
        Files.write(txtOk, bTxtOk);
        txtBroken = Files.createTempFile(tmp.toPath(), "txtBroken", null);
        Files.write(txtBroken, bTxtBroken);
    }

    // Guava will happily read file with broken "UTF-8", and replace problematic
    // code points with the Unicode Replacement Char U+FFFD plus a NUL bytes
    // (for StandardCharsets.UTF_8)
    @Test
    public void guavaToStringEncoding() throws IOException {
        String textOk = com.google.common.io.Files.toString(txtOk.toFile(), StandardCharsets.UTF_8);
        assertEquals(sTxtOk, textOk);
        String textBroken = com.google.common.io.Files.toString(txtBroken.toFile(), StandardCharsets.UTF_8);
        assertEquals(sTxtBrokenWithReplacement, textBroken);
    }

    // java.nio Files.readString() throws
    @Test
    public void jdkNioFilesReadString() throws IOException {
        String textOk = Files.readString(txtOk, StandardCharsets.UTF_8);
        assertEquals(sTxtOk, textOk);
        assertThrows(java.nio.charset.MalformedInputException.class,
                () -> Files.readString(txtBroken, StandardCharsets.UTF_8));
    }

    // java.nio Files.readAllBytes + new String(... UTF-8): same as Guava
    @Test
    public void jdkNioFileGetAllBytes() throws IOException {
        String textOk = new String(Files.readAllBytes(txtOk), StandardCharsets.UTF_8);
        assertEquals(sTxtOk, textOk);
        String textBroken = new String(Files.readAllBytes(txtBroken), StandardCharsets.UTF_8);
        assertEquals(sTxtBrokenWithReplacement, textBroken);
    }


    // java.io: same as Guava
    @Test
    public void jdkIoToStringEncoding() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(txtOk.toFile(), StandardCharsets.UTF_8))) {
            assertEquals(sTxtOk, reader.readLine());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(txtBroken.toFile(), StandardCharsets.UTF_8))) {
            assertEquals("\ufffd\u0000", reader.readLine());
        }
    }
}
