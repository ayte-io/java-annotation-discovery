package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.InterfaceReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class InterfaceSymbol implements SymbolSpec<InterfaceReference> {
    public static final InterfaceSymbol INSTANCE = new InterfaceSymbol();

    private InterfaceSymbol() {}

    @Override
    public String getSingularName() {
        return "interface";
    }

    @Override
    public Class<InterfaceReference> getReferenceType() {
        return InterfaceReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(InterfaceReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.TYPE;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.INTERFACE;
    }
}
