package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.FieldReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class FieldSymbol implements SymbolSpec<FieldReference> {
    public static final FieldSymbol INSTANCE = new FieldSymbol();

    private FieldSymbol() {}

    @Override
    public String getSingularName() {
        return "field";
    }

    @Override
    public Class<FieldReference> getReferenceType() {
        return FieldReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(FieldReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.FIELD;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.FIELD;
    }
}
