package io.ayte.utility.discovery.compilation.reader;

import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.api.v1.reference.ClassReference;
import io.ayte.utility.discovery.api.v1.reference.TypeParameterReference;
import io.ayte.utility.discovery.compilation.Reader;
import io.ayte.utility.discovery.compilation.Types;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Collections;
import java.util.Set;

public class TypeParameterReader implements Reader {
    @Override
    public SymbolCollection read(AccessibleSymbolCollection collection, Element element) {
        ClassReference<?> reference = new ClassReference<>(Types.getName(element.getEnclosingElement()));
        return collection.addTypeParameter(new TypeParameterReference<>(reference, Types.getName(element)));
    }

    @Override
    public Set<ElementKind> getProcessedElementKinds() {
        return Collections.singleton(ElementKind.TYPE_PARAMETER);
    }
}
