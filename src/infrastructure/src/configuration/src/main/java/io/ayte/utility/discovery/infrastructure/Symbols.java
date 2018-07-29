package io.ayte.utility.discovery.infrastructure;

import io.ayte.utility.discovery.infrastructure.symbol.AnnotationSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.ClassSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.EnumConstantSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.EnumSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.FieldSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.InterfaceSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.MethodArgumentSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.MethodSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.PackageSymbol;
import io.ayte.utility.discovery.infrastructure.symbol.TypeParameterSymbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Symbols {
    private Symbols() {
        // static access only
    }

    public static final List<SymbolSpec<?>> LIST = Collections.unmodifiableList(Arrays.asList(
            PackageSymbol.INSTANCE,
            TypeParameterSymbol.INSTANCE,
            AnnotationSymbol.INSTANCE,
            ClassSymbol.INSTANCE,
            InterfaceSymbol.INSTANCE,
            EnumSymbol.INSTANCE,
            EnumConstantSymbol.INSTANCE,
            FieldSymbol.INSTANCE,
            MethodSymbol.INSTANCE,
            MethodArgumentSymbol.INSTANCE
    ));

    public static SymbolSpec lookup(Class<?> reference) {
        return lookup(candidate -> candidate.getReferenceType().equals(reference));
    }

    public static SymbolSpec lookup(Predicate<SymbolSpec> predicate) {
        return LIST.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }
}
