package io.ayte.utility.discovery.api.v1;

public interface ElementReference<T> {
    String getName();
    T toSymbol() throws Exception;
}
