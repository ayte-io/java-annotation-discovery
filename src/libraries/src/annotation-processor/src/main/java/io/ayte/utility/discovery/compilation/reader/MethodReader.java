package io.ayte.utility.discovery.compilation.reader;

import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.api.v1.reference.MethodReference;
import io.ayte.utility.discovery.compilation.Reader;
import io.ayte.utility.discovery.compilation.Types;
import lombok.val;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodReader implements Reader {
    @Override
    public SymbolCollection read(AccessibleSymbolCollection collection, Element element) {
        return collection.addMethod(convert(element));
    }

    public static MethodReference<?> convert(Element element) {
        return convert((ExecutableElement) element);
    }

    public static MethodReference<?> convert(ExecutableElement element) {
        val name = Types.getName(element);
        val parent = ClassElementReader.convert(element.getEnclosingElement());
        val parameters = element.getParameters().stream()
                .map(VariableElement::getSimpleName)
                .map(Name::toString)
                .map(MethodReference.Argument::new)
                .collect(Collectors.toList());
        return new MethodReference<>(parent, name, parameters);
    }

    @Override
    public Set<ElementKind> getProcessedElementKinds() {
        return Collections.singleton(ElementKind.METHOD);
    }
}
