package com.messkhata.model;

/**
 * Anything that can be saved as one line in a data file.
 */
public interface Storable {

    String SEPARATOR = "|";

    String toLine();
}
