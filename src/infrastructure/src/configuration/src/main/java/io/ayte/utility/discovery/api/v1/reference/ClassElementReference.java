package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.TopElementReference;
import lombok.Data;

@Data
public abstract class ClassElementReference<T> implements TopElementReference<Class<T>> {
    private final String name;

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> toSymbol() throws ClassNotFoundException {
        return (Class<T>) Class.forName(name);
    }
}
