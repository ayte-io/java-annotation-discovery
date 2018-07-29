package io.ayte.utility.discovery.api.v1.reference;

public class EnumReference<T extends Enum<T>> extends ClassElementReference<T> {
    public EnumReference(String name) {
        super(name);
    }
}
