package io.ayte.utility.discovery.compilation.reader;

import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.api.v1.reference.AnnotationReference;
import io.ayte.utility.discovery.api.v1.reference.ClassElementReference;
import io.ayte.utility.discovery.api.v1.reference.ClassReference;
import io.ayte.utility.discovery.api.v1.reference.EnumReference;
import io.ayte.utility.discovery.api.v1.reference.InterfaceReference;
import io.ayte.utility.discovery.compilation.Reader;
import io.ayte.utility.discovery.compilation.Types;
import lombok.val;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassElementReader implements Reader {
    @Override
    public SymbolCollection read(AccessibleSymbolCollection collection, Element element) {
        val kind = element.getKind();
        val name = Types.getName(element);
        switch (kind) {
            case CLASS:
                return collection.addClass(new ClassReference<>(name));
            case INTERFACE:
                return collection.addInterface(new InterfaceReference<>(name));
            case ENUM:
                return collection.addEnum(new EnumReference<>(name));
            case ANNOTATION_TYPE:
                return collection.addAnnotation(new AnnotationReference<>(name));
            default:
                throw new RuntimeException("Unexpected element kind: " + kind + ", element: " + element);
        }
    }

    @Override
    public Set<ElementKind> getProcessedElementKinds() {
        return Stream.of(ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE, ElementKind.ENUM)
                .collect(Collectors.toSet());
    }

    public static ClassElementReference<?> convert(Element element) {
        val kind = element.getKind();
        val name = Types.getName(element);
        switch (kind) {
            case CLASS:
                return new ClassReference<>(name);
            case INTERFACE:
                return new InterfaceReference<>(name);
            case ENUM:
                return new EnumReference<>(name);
            case ANNOTATION_TYPE:
                return new AnnotationReference<>(name);
            default:
                throw new RuntimeException("Unexpected element kind: " + kind + ", element: " + element);
        }
    }
}
