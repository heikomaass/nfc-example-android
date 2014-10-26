package de.heikomaass.example.nfctest;

import android.nfc.NdefRecord;

import java.nio.charset.Charset;

import static java.lang.System.arraycopy;

/**
 * Created by hmaass on 25.10.14.
 */
public class TextRecord {

    private final Charset encoding;
    private final String languageCode;
    private final String text;

    private TextRecord(Charset encoding, String languageCode, String text) {
        this.encoding = encoding;
        this.languageCode = languageCode;
        this.text = text;
    }

    public static class Builder {

        private NdefRecord record;

        public Builder(NdefRecord record) {
            this.record = record;
        }

        public TextRecord build() {
            byte[] payload = record.getPayload();
            if (payload.length == 0) {
                throw new IllegalArgumentException("NdefRecord does not contain payload");
            }
            byte statusByte = payload[0];

            // The status byte contains encoding and the length of languageCode
            Charset encoding = this.extractCharset(statusByte);
            String languageCode = this.extractLanguageCode(statusByte, payload, encoding);
            String text = this.extractText(statusByte, payload, encoding);

            return new TextRecord(encoding, languageCode, text);
        }

        private Charset extractCharset(byte statusByte) {
            // 0000 0000 means UTF-8
            // 1000 0000 means UTF-16
            int encoded = statusByte & 0x80;
            if (encoded == 0) {
                return Charset.forName("UTF-8");
            } else {
                return Charset.forName("UTF-16");
            }
        }

        private String extractLanguageCode(byte statusByte, byte[] payload, Charset encoding) {
            byte lengthOfLanguageCode = this.extractLengthOfLanguageCode(statusByte);

            byte[] languageCodeBytes = new byte[lengthOfLanguageCode];
            arraycopy(payload, 1, languageCodeBytes, 0, lengthOfLanguageCode);
            return new String(languageCodeBytes, encoding);
        }

        private String extractText(byte statusByte, byte[] payload, Charset encoding) {
            byte lengthOfLanguageCode = this.extractLengthOfLanguageCode(statusByte);

            byte[] textBytes = new byte[payload.length - lengthOfLanguageCode - 1];
            arraycopy(payload, 1 + lengthOfLanguageCode, textBytes, 0, textBytes.length);
            return new String(textBytes, encoding);
        }

        private byte extractLengthOfLanguageCode(byte statusByte) {
            // 000x xxxx contains the length of status code
            return (byte) (statusByte & 0x1F);
        }
    }

    public Charset getEncoding() {
        return encoding;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getText() {
        return text;
    }


}
