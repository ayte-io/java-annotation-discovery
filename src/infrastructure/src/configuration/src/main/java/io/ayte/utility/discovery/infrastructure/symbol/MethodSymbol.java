package io.ayte.utility.discovery.infrastructure.symbol;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.reference.MethodReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.Symbols;
import io.ayte.utility.discovery.infrastructure.generation.TypeNames;
import lombok.val;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class MethodSymbol implements SymbolSpec<MethodReference> {
    public static final MethodSymbol INSTANCE = new MethodSymbol();

    private MethodSymbol() {}

    @Override
    public String getSingularName() {
        return "method";
    }

    @Override
    public Class<MethodReference> getReferenceType() {
        return MethodReference.class;
    }

    @Override
    public TypeName getGenerationType() {
        return TypeNames.wildcard(MethodReference.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CodeBlock emitConstructor(MethodReference reference) {
        val arguments = reference.getArguments().stream()
                .map(name -> CodeBlock.of("new $T($S)", MethodReference.Argument.class, name))
                .collect(CodeBlock.joining(", "));
        val parent = reference.getParent();
        val parentConstructor = Symbols.lookup(parent.getClass()).emitConstructor(parent);
        return CodeBlock.of("new $T($L, $S, $L)",
                MethodReference.class,
                parentConstructor,
                reference.getName(),
                arguments
        );
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.METHOD;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.METHOD;
    }
}
