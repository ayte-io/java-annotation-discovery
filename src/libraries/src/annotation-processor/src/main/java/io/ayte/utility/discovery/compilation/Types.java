package io.ayte.utility.discovery.compilation;

import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;

public class Types {
    private Types() {
        // static access only
    }

    public static String getName(Element element) {
        if (element instanceof QualifiedNameable) {
            return getName((QualifiedNameable) element);
        }
        return element.getSimpleName().toString();
    }

    public static String getName(QualifiedNameable element) {
        return element.getQualifiedName().toString();
    }
}
