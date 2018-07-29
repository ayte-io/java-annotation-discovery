package io.ayte.utility.discovery.compilation.reader;

import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.api.v1.reference.EnumConstantReference;
import io.ayte.utility.discovery.api.v1.reference.EnumReference;
import io.ayte.utility.discovery.compilation.Reader;
import io.ayte.utility.discovery.compilation.Types;
import lombok.val;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Collections;
import java.util.Set;

public class EnumConstantReader implements Reader {
    @Override
    public SymbolCollection read(AccessibleSymbolCollection collection, Element element) {
        val reference = (EnumReference<?>) ClassElementReader.convert(element.getEnclosingElement());
        return collection.addEnumConstant(new EnumConstantReference<>(reference, Types.getName(element)));
    }

    @Override
    public Set<ElementKind> getProcessedElementKinds() {
        return Collections.singleton(ElementKind.ENUM_CONSTANT);
    }
}
