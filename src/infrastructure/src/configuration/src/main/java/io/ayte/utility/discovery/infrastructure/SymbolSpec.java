package io.ayte.utility.discovery.infrastructure;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.ayte.utility.discovery.api.v1.ElementReference;
import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.val;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public interface SymbolSpec<T extends ElementReference> {
    String getSingularName();
    default String getPluralName() {
        return getSingularName() + 's';
    }
    Class<T> getReferenceType();
    default TypeName getGenerationType() {
        return ClassName.get(getReferenceType());
    }
    ElementType getAnnotationTarget();
    ElementKind getElementKind();
    @SuppressWarnings("unchecked")
    default CodeBlock emitConstructor(T reference) {
        if (reference instanceof InnerElementReference) {
            val casted = (InnerElementReference) reference;
            val parent = casted.getParent();
            val parentConstructor = Symbols.lookup(parent.getClass()).emitConstructor(parent);
            return CodeBlock.of("new $T($L, $S)", getReferenceType(), parentConstructor, casted.getName());
        }
        return CodeBlock.of("new $T($S)", getReferenceType(), reference.getName());
    }
}
