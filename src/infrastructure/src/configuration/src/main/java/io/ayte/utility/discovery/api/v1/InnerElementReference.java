package io.ayte.utility.discovery.api.v1;

public interface InnerElementReference<R extends ElementReference, T> extends ElementReference<T> {
    R getParent();
    String getName();
}
