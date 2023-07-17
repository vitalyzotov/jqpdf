package ru.vzotov.jqpdf.domain;


public abstract class AbstractRefOrName<T, R> implements RefOrName<T, R> {

    private final String ref;

    private final T name;

    public AbstractRefOrName(String ref, T name) {
        this.ref = ref;
        this.name = name;
    }

    @Override
    public String ref() {
        return ref;
    }

    @Override
    public T name() {
        return name;
    }
}
