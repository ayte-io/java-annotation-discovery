package io.ayte.utility.discovery.infrastructure.generation;

import java.util.Collection;

public interface CodeGenerator<C> {
    Collection<CodeSource> generate(C context);
}
