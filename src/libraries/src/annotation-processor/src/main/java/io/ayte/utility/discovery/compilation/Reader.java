package io.ayte.utility.discovery.compilation;

import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.SymbolCollection;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Set;

public interface Reader {
    SymbolCollection read(AccessibleSymbolCollection collection, Element element);
    Set<ElementKind> getProcessedElementKinds();
}
