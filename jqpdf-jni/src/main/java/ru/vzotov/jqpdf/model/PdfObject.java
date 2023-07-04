package ru.vzotov.jqpdf.model;

public record PdfObject(
        Object value,
        PdfStream stream
) {
}
