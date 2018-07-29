package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.EnumConstantReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class EnumConstantSymbol implements SymbolSpec<EnumConstantReference> {
    public static final EnumConstantSymbol INSTANCE = new EnumConstantSymbol();

    private EnumConstantSymbol() {}

    @Override
    public String getSingularName() {
        return "enumConstant";
    }

    @Override
    public Class<EnumConstantReference> getReferenceType() {
        return EnumConstantReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(EnumConstantReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.FIELD;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.ENUM_CONSTANT;
    }
}
