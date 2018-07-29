package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.TypeParameterReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class TypeParameterSymbol implements SymbolSpec<TypeParameterReference> {
    public static final TypeParameterSymbol INSTANCE = new TypeParameterSymbol();

    private TypeParameterSymbol() {}

    @Override
    public String getSingularName() {
        return "typeParameter";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<TypeParameterReference> getReferenceType() {
        return TypeParameterReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(TypeParameterReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.TYPE_PARAMETER;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.TYPE_PARAMETER;
    }
}
