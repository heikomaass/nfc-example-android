package de.heikomaass.example.nfctest;

import android.nfc.NdefRecord;
import android.test.InstrumentationTestCase;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by hmaass on 25.10.14.
 */
public class TextRecordTest extends InstrumentationTestCase{

    public void testBuild_should_detectUTF8() throws UnsupportedEncodingException {
        NdefRecord ndefRecord = this.createNdefRecord("de-DE", Charset.forName("UTF-8"), "Banana2000");

        TextRecord textRecord = new TextRecord.Builder(ndefRecord).build();
        assertEquals("UTF-8", textRecord.getEncoding().displayName());
    }

    public void testBuild_should_detectUTF16() throws UnsupportedEncodingException {
        NdefRecord ndefRecord = this.createNdefRecord("de-DE", Charset.forName("UTF-16"), "Banana2000");

        TextRecord textRecord = new TextRecord.Builder(ndefRecord).build();
        assertEquals("UTF-16", textRecord.getEncoding().displayName());
    }

    public void testBuild_should_detectLanguageCode() throws UnsupportedEncodingException {
        NdefRecord ndefRecord = this.createNdefRecord("de-DE", Charset.forName("UTF-16"), "Banana2000");

        TextRecord textRecord = new TextRecord.Builder(ndefRecord).build();
        assertEquals("de-DE", textRecord.getLanguageCode());
    }

    public void testBuild_should_detectText() throws UnsupportedEncodingException {
        NdefRecord ndefRecord = this.createNdefRecord("de-DE", Charset.forName("UTF-16"), "Banana2000");

        TextRecord textRecord = new TextRecord.Builder(ndefRecord).build();
        assertEquals("Banana2000", textRecord.getText());
    }

    private NdefRecord createNdefRecord(String languageCode, Charset charset, String text) throws UnsupportedEncodingException {
        // The method NdefRecord.createTextRecord is only available in API level 21.
        // So I've to construct a text NdefRecord on my own.

        byte[] languageCodeBytes = languageCode.getBytes(charset);
        byte encoding = 0;
        if ("UTF-16".equals(charset.displayName())) {
            encoding = (byte) 128;
        }
        byte statusByte = (byte) (encoding | languageCodeBytes.length);
        byte[] textBytes = text.getBytes(charset);

        byte[] payload = new byte[1 + languageCodeBytes.length + textBytes.length];
        payload[0] = statusByte;
        System.arraycopy(languageCodeBytes, 0, payload, 1, languageCodeBytes.length);
        System.arraycopy(textBytes, 0, payload, 1+languageCodeBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,new byte[0],new byte[0], payload);
    }
}
