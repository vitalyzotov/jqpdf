package ru.vzotov.jqpdf;

public class JQPDF {

    static {
        System.loadLibrary("jqpdflib");
    }

    public interface Callback {
        void callback(byte[] data);
    }

    public native int pdfToJson(byte[] inData, Callback callback);
}
