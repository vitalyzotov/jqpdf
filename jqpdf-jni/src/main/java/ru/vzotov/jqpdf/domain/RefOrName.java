package ru.vzotov.jqpdf.domain;

public interface RefOrName<T, R> {
    String ref();
    T name();
    R resolve();
}
