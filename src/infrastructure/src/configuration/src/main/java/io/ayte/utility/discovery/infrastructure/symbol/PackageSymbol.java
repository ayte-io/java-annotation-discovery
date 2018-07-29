package io.ayte.utility.discovery.infrastructure.symbol;

import io.ayte.utility.discovery.api.v1.reference.PackageReference;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.ElementType;

public class PackageSymbol implements SymbolSpec<PackageReference> {
    public static final PackageSymbol INSTANCE = new PackageSymbol();

    private PackageSymbol() {}

    @Override
    public String getSingularName() {
        return "package";
    }

    @Override
    public Class<PackageReference> getReferenceType() {
        return PackageReference.class;
    }

    @Override
    public ElementType getAnnotationTarget() {
        return ElementType.PACKAGE;
    }

    @Override
    public ElementKind getElementKind() {
        return ElementKind.PACKAGE;
    }
}
