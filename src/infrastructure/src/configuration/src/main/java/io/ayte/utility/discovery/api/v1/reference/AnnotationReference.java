package io.ayte.utility.discovery.api.v1.reference;

import java.lang.annotation.Annotation;

public class AnnotationReference<T extends Annotation> extends ClassElementReference<T> {
    public AnnotationReference(String name) {
        super(name);
    }
}
