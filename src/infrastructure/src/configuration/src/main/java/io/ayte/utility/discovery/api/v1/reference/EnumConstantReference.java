package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.Data;

@Data
public class EnumConstantReference<T extends Enum<T>> implements InnerElementReference<EnumReference<T>, T> {
    private final EnumReference<T> parent;
    private final String name;

    @Override
    public T toSymbol() throws ClassNotFoundException {
        return Enum.valueOf(parent.toSymbol(), name);
    }
}
