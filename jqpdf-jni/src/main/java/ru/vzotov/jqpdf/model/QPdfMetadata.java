package ru.vzotov.jqpdf.model;

public record QPdfMetadata(
        int jsonversion,
        String pdfversion,
        boolean pushedinheritedpageresources,
        boolean calledgetallpages,
        int maxobjectid
) implements QPdfObject {
}
