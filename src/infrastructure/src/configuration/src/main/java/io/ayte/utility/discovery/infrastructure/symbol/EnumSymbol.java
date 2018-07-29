package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.EnumReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class EnumSymbol implements SymbolSpec<EnumReference> {
    public static final EnumSymbol INSTANCE = new EnumSymbol();

    private EnumSymbol() {}

    @Override
    public String getSingularName() {
        return "enum";
    }

    @Override
    public Class<EnumReference> getReferenceType() {
        return EnumReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(EnumReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.TYPE;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.ENUM;
    }
}
