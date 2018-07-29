package io.ayte.utility.discovery.infrastructure.generation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import lombok.val;

import javax.lang.model.element.Modifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;

public class AutoDiscoveryAnnotationGenerator implements CodeGenerator<List<SymbolSpec>> {
    @Override
    public Collection<CodeSource> generate(List<SymbolSpec> symbols) {
        val doc = "This annotation acts as a discovery root. Every element marked with\n" +
                "this annotation will be automatically found and recorded during\n" +
                "annotation processing stage of compilation.\n\n" +
                "@see Repository\n";
        val type = TypeSpec.annotationBuilder(Constants.AUTO_DISCOVERY_NAME)
                .addJavadoc(doc)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(createTargetAnnotation(symbols))
                .addAnnotation(createRetentionAnnotation())
                .addMethod(createTransitiveMethod())
                .build();
        return CodeSource.singleton(type, Constants.AUTO_DISCOVERY_PACKAGE);
    }

    private static AnnotationSpec createTargetAnnotation(List<SymbolSpec> symbols) {
        val targets = symbols.stream()
                .map(SymbolSpec::getAnnotationTarget)
                .distinct()
                .map(target -> CodeBlock.of("$T.$L", ElementType.class, target.toString()))
                .collect(CodeBlock.joining(",\n", "{\n", "\n}"));
        return AnnotationSpec.builder(Target.class)
                .addMember("value", targets)
                .build();
    }

    private static AnnotationSpec createRetentionAnnotation() {
        return AnnotationSpec.builder(Retention.class)
                .addMember("value", CodeBlock.of("$T.RUNTIME", RetentionPolicy.class))
                .build();
    }

    private static MethodSpec createTransitiveMethod() {
        val doc = "This method allows to mark another annotation as discovery root. " +
                "All annotations marked <code>@AutoDiscovery(transitive = true)</code>" +
                "will be treated as <code>@AutoDiscovery</code> itself.";
        return MethodSpec.methodBuilder("transitive")
                .addJavadoc(doc)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.BOOLEAN)
                .defaultValue("false")
                .build();
    }
}
