package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.MethodArgumentReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class MethodArgumentSymbol implements SymbolSpec<MethodArgumentReference> {
    public static final MethodArgumentSymbol INSTANCE = new MethodArgumentSymbol();

    private MethodArgumentSymbol() {}

    @Override
    public String getSingularName() {
        return "methodArgument";
    }

    @Override
    public Class<MethodArgumentReference> getReferenceType() {
        return MethodArgumentReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(MethodArgumentReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.PARAMETER;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.PARAMETER;
    }
}
