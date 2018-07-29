package io.ayte.utility.discovery.compilation.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.ayte.utility.discovery.api.v1.ElementReference;
import io.ayte.utility.discovery.api.v1.Repository;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.CodeGenerator;
import io.ayte.utility.discovery.infrastructure.generation.CodeSource;
import io.ayte.utility.discovery.infrastructure.misc.Strings;
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
import lombok.val;

import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PopulatedRepositoryGenerator implements CodeGenerator<Map<String, ? extends SymbolCollection>> {
    public static final String ARGUMENT_NAME = "annotation";

    @Override
    public Collection<CodeSource> generate(Map<String, ? extends SymbolCollection> context) {
        return CodeSource.singleton(generateSpec(context), Constants.POPULATED_REPOSITORY_PACKAGE);
    }

    public static TypeSpec generateSpec(Map<String, ? extends SymbolCollection> elements) {
        val builder = TypeSpec.classBuilder(Constants.POPULATED_REPOSITORY_NAME)
                .addSuperinterface(ClassName.get(Repository.class))
                .addModifiers(Modifier.PUBLIC);
        generateFields().forEach(builder::addField);
        generateGetters().forEach(builder::addMethod);
        generateSetters().forEach(builder::addMethod);
        val body = elements.entrySet().stream()
                .map(entry -> processCollection(entry.getKey(), entry.getValue()))
                .collect(CodeBlock.joining("\n"));
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode(body)
                .build();
        return builder.addMethod(constructor).build();
    }

    private static List<FieldSpec> generateFields() {
        return Constants.PROCESSED_ELEMENTS.stream()
                .flatMap(element -> {
                    val setFieldType = ParameterizedTypeName
                            .get(ClassName.get(Set.class), element.getGenerationType());
                    val setField = FieldSpec.builder(setFieldType, element.getSingularName() + "Set")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("new $T<>()", HashSet.class)
                            .build();
                    val mapFieldType = ParameterizedTypeName
                            .get(ClassName.get(Map.class), ClassName.get(String.class), setFieldType);
                    val mapField = FieldSpec.builder(mapFieldType, element.getSingularName() + "Map")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("new $T<>()", HashMap.class)
                            .build();
                    return Stream.of(mapField, setField);
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("CodeBlock2Expr")
    private static List<MethodSpec> generateGetters() {
        return Constants.PROCESSED_ELEMENTS.stream()
                .flatMap(element -> {
                    val returnType = ParameterizedTypeName
                            .get(ClassName.get(Set.class), element.getGenerationType());
                    val methodName = "get" + Strings.capitalize(element.getPluralName());
                    val setFieldName = element.getSingularName() + "Set";
                    val mapFieldName = element.getSingularName() + "Map";
                    val allGetter = MethodSpec.methodBuilder(methodName)
                            .addStatement("return new $T<>($L)", HashSet.class, setFieldName);
                    val annotationType = ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(Annotation.class))
                    );
                    val annotationGetter = MethodSpec.methodBuilder(methodName)
                            .addParameter(ParameterSpec.builder(annotationType, ARGUMENT_NAME).build())
                            .addStatement("return $L(annotation.getCanonicalName())", methodName);
                    val stringGetter = MethodSpec.methodBuilder(methodName)
                            .addParameter(ParameterSpec.builder(ClassName.get(String.class), ARGUMENT_NAME).build())
                            .addStatement(
                                    "return $L.getOrDefault(annotation, $T.emptySet())",
                                    mapFieldName,
                                    Collections.class
                            );
                    return Stream
                            .of(allGetter, annotationGetter, stringGetter)
                            .map(builder -> builder.returns(returnType));
                })
                .map(builder -> {
                    return builder
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static List<MethodSpec> generateSetters() {
        return Constants.PROCESSED_ELEMENTS.stream()
                .map(element -> {
                    val annotation = ParameterSpec.builder(ClassName.get(String.class), ARGUMENT_NAME).build();
                    val reference = ParameterSpec.builder(element.getReferenceType(), "reference").build();
                    return MethodSpec.methodBuilder("add" + Strings.capitalize(element.getSingularName()))
                            .addModifiers(Modifier.PRIVATE)
                            .addParameter(annotation)
                            .addParameter(reference)
                            .addStatement("$LMap.get($N).add($N)", element.getSingularName(), annotation, reference)
                            .addStatement("$LSet.add($N)", element.getSingularName(), reference)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static CodeBlock processCollection(String annotation, SymbolCollection elements) {
        return Stream
                .of(
                        initMaps(annotation),
                        elements.getPackages().stream()
                                .map(element -> process(annotation, PackageSymbol.INSTANCE, element)),
                        elements.getClasses().stream()
                                .map(element -> process(annotation, ClassSymbol.INSTANCE, element)),
                        elements.getInterfaces().stream()
                                .map(element -> process(annotation, InterfaceSymbol.INSTANCE, element)),
                        elements.getAnnotations().stream()
                                .map(element -> process(annotation, AnnotationSymbol.INSTANCE, element)),
                        elements.getEnums().stream()
                                .map(element -> process(annotation, EnumSymbol.INSTANCE, element)),
                        elements.getEnumConstants().stream()
                                .map(element -> process(annotation, EnumConstantSymbol.INSTANCE, element)),
                        elements.getTypeParameters().stream()
                                .map(element -> process(annotation, TypeParameterSymbol.INSTANCE, element)),
                        elements.getFields().stream()
                                .map(element -> process(annotation, FieldSymbol.INSTANCE, element)),
                        elements.getMethods().stream()
                                .map(element -> process(annotation, MethodSymbol.INSTANCE, element)),
                        elements.getMethodArguments().stream()
                                .map(element -> process(annotation, MethodArgumentSymbol.INSTANCE, element))
                )
                .flatMap(Function.identity())
                .collect(CodeBlock.joining("\n", "", "\n"));
    }

    private static Stream<CodeBlock> initMaps(String annotation) {
        return Constants.PROCESSED_ELEMENTS.stream()
                .map(element -> {
                    val field = element.getSingularName() + "Map";
                    return CodeBlock.of("$L.put($S, new $T<>());", field, annotation, HashSet.class);
                });
    }

    private static <T extends ElementReference<?>> CodeBlock process(String annotation, SymbolSpec<T> spec, T reference) {
        val method = "add" + Strings.capitalize(spec.getSingularName());
        val constructor = spec.emitConstructor(reference);
        return CodeBlock.of("$L($S, $L);", method, annotation, constructor);
    }
}
