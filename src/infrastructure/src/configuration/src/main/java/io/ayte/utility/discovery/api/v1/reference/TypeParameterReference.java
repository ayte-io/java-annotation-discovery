package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.Data;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;

@Data
public class TypeParameterReference<P> implements InnerElementReference<ClassElementReference<?>, TypeVariable<?>> {
    private final ClassElementReference<P> parent;
    private final String name;

    @Override
    public TypeVariable<?> toSymbol() throws ClassNotFoundException {
        return Arrays.stream(parent.toSymbol().getTypeParameters())
                .filter(parameter -> parameter.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
