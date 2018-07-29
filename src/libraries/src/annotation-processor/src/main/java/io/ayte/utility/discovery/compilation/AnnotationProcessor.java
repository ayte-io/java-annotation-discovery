package io.ayte.utility.discovery.compilation;

import com.google.auto.service.AutoService;
import io.ayte.utility.discovery.api.v1.AccessibleSymbolCollection;
import io.ayte.utility.discovery.api.v1.AutoDiscovery;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.compilation.emitter.SPIEmitter;
import io.ayte.utility.discovery.compilation.emitter.YamlEmitter;
import io.ayte.utility.discovery.compilation.reader.ClassElementReader;
import io.ayte.utility.discovery.compilation.reader.EnumConstantReader;
import io.ayte.utility.discovery.compilation.reader.FieldReader;
import io.ayte.utility.discovery.compilation.reader.MethodArgumentReader;
import io.ayte.utility.discovery.compilation.reader.MethodReader;
import io.ayte.utility.discovery.compilation.reader.PackageReader;
import io.ayte.utility.discovery.compilation.reader.ParameterReader;
import io.ayte.utility.discovery.compilation.reader.TypeParameterReader;
import lombok.val;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SuppressWarnings("CodeBlock2Expr")
public class AnnotationProcessor extends AbstractProcessor {
    private static final List<Reader> READERS = Arrays.asList(
            new PackageReader(),
            new ClassElementReader(),
            new TypeParameterReader(),
            new FieldReader(),
            new MethodReader(),
            new MethodArgumentReader(),
            new ParameterReader(),
            new EnumConstantReader()
    );

    private static final List<ArtifactEmitter> EMITTERS = Arrays.asList(
            new YamlEmitter(),
            new SPIEmitter()
    );

    private Map<String, AccessibleSymbolCollection> accumulator = new HashMap<>();

    private Filer filer;
    private Messager messager;


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        messager = environment.getMessager();
        filer = environment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoDiscovery.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> ignored, RoundEnvironment environment) {
        val extraAnnotations = findExtraAnnotations(environment);
        val elements = findElements(environment, extraAnnotations);
        val processed = process(elements, messager);
        if (!environment.processingOver()) {
            accumulator = AccessibleSymbolCollection.merge(accumulator, processed);
        } else {
            write(accumulator);
        }
        return false;
    }

    private static Set<TypeElement> findExtraAnnotations(RoundEnvironment environment) {
        return environment
                .getElementsAnnotatedWith(AutoDiscovery.class)
                .stream()
                .filter(element -> element.getKind().equals(ElementKind.ANNOTATION_TYPE))
                .map(element -> (TypeElement) element)
                .filter(element -> {
                    AutoDiscovery annotation = element.getAnnotation(AutoDiscovery.class);
                    return annotation != null && annotation.transitive();
                })
                .collect(Collectors.toSet());
    }

    private static Map<String, Set<? extends Element>> findElements(
            RoundEnvironment environment,
            Set<TypeElement> annotations
    ) {
        Map<String, Set<? extends Element>> target = new HashMap<>();
        target.put(AutoDiscovery.class.getCanonicalName(), environment.getElementsAnnotatedWith(AutoDiscovery.class));
        annotations.forEach(annotation -> {
            val elements = environment.getElementsAnnotatedWith(annotation);
            val name = annotation.getQualifiedName().toString();
            target.put(name, elements);
        });
        return target;
    }

    private static Map<String, AccessibleSymbolCollection> process(
            Map<String, Set<? extends Element>> elements,
            Messager messager
    ) {
        return elements.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    val collection = new AccessibleSymbolCollection();
                    entry.getValue().forEach(element -> process(collection, element, messager));
                    return collection;
                }));
    }

    private static SymbolCollection process(AccessibleSymbolCollection collection, Element element, Messager messager) {
        val result = READERS.stream()
                .filter(reader -> reader.getProcessedElementKinds().contains(element.getKind()))
                .findFirst()
                .<SymbolCollection>map(reader -> reader.read(collection, element));
        if (result.isPresent()) {
            return result.get();
        }
        messager.printMessage(Diagnostic.Kind.WARNING, "This language element is not yet supported", element);
        return collection;
    }

    private void write(Map<String, AccessibleSymbolCollection> elements) {
        for (val emitter : EMITTERS) {
            try {
                for (val artifact : emitter.emit(elements)) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Writing artifact " + artifact);
                    write(artifact);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to write recorded elements", e);
            }
        }
    }

    private void write(ArtifactEmitter.Artifact artifact) throws IOException {
        switch (artifact.getType()) {
            case CLASS:
                writeClass(artifact);
                break;
            case SOURCE:
                writeSource(artifact);
                break;
            default:
                writeResource(artifact);
        }
    }

    private void writeClass(ArtifactEmitter.Artifact artifact) throws IOException {
        pipe(artifact.getContent(), filer.createClassFile(artifact.getPath()));
    }

    private void writeResource(ArtifactEmitter.Artifact artifact) throws IOException {
        val resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", artifact.getPath());
        pipe(artifact.getContent(), resource);
    }

    private void writeSource(ArtifactEmitter.Artifact artifact) throws IOException {
        pipe(artifact.getContent(), filer.createSourceFile(artifact.getPath()));
    }

    private void pipe(InputStream input, FileObject target) throws IOException {
        int next;
        try (val output = target.openOutputStream()) {
            while ((next = input.read()) != -1) {
                output.write(next);
            }
        }
    }
}

