package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.Data;

import java.lang.reflect.Field;

@Data
public class FieldReference<T> implements InnerElementReference<ClassElementReference<T>, Field> {
    private final ClassElementReference<T> parent;
    private final String name;

    @Override
    public Field toSymbol() throws ClassNotFoundException, NoSuchFieldException {
        return parent.toSymbol().getField(name);
    }
}
