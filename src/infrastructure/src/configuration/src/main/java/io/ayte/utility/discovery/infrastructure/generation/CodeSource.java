package io.ayte.utility.discovery.infrastructure.generation;

import com.squareup.javapoet.TypeSpec;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class CodeSource {
    private final TypeSpec spec;
    private final String packageName;

    public static CodeSource of(TypeSpec spec, String packageName) {
        return new CodeSource(spec, packageName);
    }

    public static Set<CodeSource> singleton(CodeSource source) {
        return Collections.singleton(source);
    }

    public static Set<CodeSource> singleton(TypeSpec spec, String packageName) {
        return singleton(of(spec, packageName));
    }
}
