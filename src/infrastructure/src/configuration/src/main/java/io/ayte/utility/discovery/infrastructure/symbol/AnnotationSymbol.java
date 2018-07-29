package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.AnnotationReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class AnnotationSymbol implements SymbolSpec<AnnotationReference> {
    public static final AnnotationSymbol INSTANCE = new AnnotationSymbol();

    private AnnotationSymbol() {}

    @Override
    public String getSingularName() {
        return "annotation";
    }

    @Override
    public Class<AnnotationReference> getReferenceType() {
        return AnnotationReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(AnnotationReference.class);
    }

    @Override
    public CodeBlock emitConstructor(AnnotationReference reference) {
        return CodeBlock.of("new $T<>($S)", AnnotationReference.class, reference.getName());
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.ANNOTATION_TYPE;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.ANNOTATION_TYPE;
    }
}
