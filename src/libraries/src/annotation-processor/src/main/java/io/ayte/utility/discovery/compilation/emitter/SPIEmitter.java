package io.ayte.utility.discovery.compilation.emitter;

import com.squareup.javapoet.JavaFile;
import io.ayte.utility.discovery.api.v1.Repository;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.compilation.ArtifactEmitter;
import io.ayte.utility.discovery.compilation.generation.PopulatedRepositoryGenerator;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.generation.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPIEmitter implements ArtifactEmitter {
    public static final String GENERATED_CLASS_NAME
            = Constants.GENERATED_PACKAGE + '.' + Constants.POPULATED_REPOSITORY_NAME;

    private final CodeGenerator<Map<String, ? extends SymbolCollection>> generator = new PopulatedRepositoryGenerator();

    @Override
    public List<Artifact> emit(Map<String, ? extends SymbolCollection> symbols) throws IOException {
        return Stream.concat(Stream.of(createConfiguration()), createImplementation(symbols).stream())
                .collect(Collectors.toList());
    }

    private static Artifact createConfiguration() {
        val path = "META-INF/services/" + Repository.class.getCanonicalName();
        return Artifact.createResource(path, GENERATED_CLASS_NAME);
    }

    private List<Artifact> createImplementation(Map<String, ? extends SymbolCollection> symbols) throws IOException {
        val accumulator = new ArrayList<Artifact>();
        for (val source : generator.generate(symbols)) {
            val writer = new StringWriter();
            JavaFile.builder(source.getPackageName(), source.getSpec())
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build()
                    .writeTo(writer);
            val path = source.getPackageName() + '.' + source.getSpec().name;
            accumulator.add(Artifact.createSource(path, writer.getBuffer().toString()));
        }
        return accumulator;
    }

}
