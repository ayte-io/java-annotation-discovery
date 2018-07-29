package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.ClassReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class ClassSymbol implements SymbolSpec<ClassReference> {
    public static final ClassSymbol INSTANCE = new ClassSymbol();

    private ClassSymbol() {}

    @Override
    public String getSingularName() {
        return "class";
    }

    @Override
    public String getPluralName() {
        return "classes";
    }

    @Override
    public Class<ClassReference> getReferenceType() {
        return ClassReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(ClassReference.class);
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.TYPE;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.CLASS;
    }
}
