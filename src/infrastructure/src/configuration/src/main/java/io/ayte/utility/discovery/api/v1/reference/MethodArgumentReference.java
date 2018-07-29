package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.Data;

import java.lang.reflect.Parameter;
import java.util.Arrays;

@Data
public class MethodArgumentReference<T> implements InnerElementReference<MethodReference<T>, Parameter> {
    private final MethodReference<T> parent;
    private final String name;

    @Override
    public Parameter toSymbol() throws ClassNotFoundException, NoSuchMethodException {
        return Arrays.stream(parent.toSymbol().getParameters())
                .filter(parameter -> parameter.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
