package ru.vzotov.jqpdf.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.DEDUCTION;

@JsonAutoDetect(fieldVisibility = ANY)
@JsonTypeInfo(use = DEDUCTION, defaultImpl = QPdfObjects.class)
@JsonSubTypes({@JsonSubTypes.Type(QPdfMetadata.class), @JsonSubTypes.Type(QPdfObjects.class)})
public interface QPdfObject {
}
