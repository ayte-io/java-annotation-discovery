package io.ayte.utility.discovery.infrastructure.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.misc.Strings;
import lombok.val;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryInfrastructureGenerator implements CodeGenerator<List<SymbolSpec>> {
    private static final String ANNOTATION_ARGUMENT_NAME = "annotation";

    @Override
    public Collection<CodeSource> generate(List<SymbolSpec> symbols) {
        return Arrays.asList(
                RepositoryInterfaceGenerator.generate(symbols),
                MergingRepositoryClassGenerator.generate(symbols),
                CachingRepositoryClassGenerator.generate(symbols)
        );
    }

    private static class RepositoryInterfaceGenerator {
        public static CodeSource generate(List<SymbolSpec> symbols) {
            val type = TypeSpec.interfaceBuilder(Constants.REPOSITORY_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethods(createGetters(symbols))
                    .addMethod(createInstancesFactory())
                    .addMethod(createInstanceFactory())
                    .addMethod(createCheckerMethod())
                    .build();
            return CodeSource.of(type, Constants.REPOSITORY_PACKAGE);
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .flatMap(RepositoryInterfaceGenerator::createSymbolGetters)
                    .collect(Collectors.toList());
        }

        private static Stream<MethodSpec> createSymbolGetters(SymbolSpec symbol) {
            val annotationParameter = ParameterSpec.builder(TypeNames.ANNOTATION_CLASS, ANNOTATION_ARGUMENT_NAME)
                    .build();
            val stringParameter = ParameterSpec.builder(TypeNames.STRING, ANNOTATION_ARGUMENT_NAME).build();
            val name = Strings.getterOf(symbol.getPluralName());
            return Stream.of(
                    createGetter(name, symbol.getGenerationType()).build(),
                    createGetter(name, symbol.getGenerationType()).addParameter(annotationParameter).build(),
                    createGetter(name, symbol.getGenerationType()).addParameter(stringParameter).build()
            );
        }

        private static MethodSpec createInstancesFactory() {
            val body = CodeBlock.builder()
                    .addStatement("return $T.load($T.class)", ServiceLoader.class, TypeNames.REPOSITORY)
                    .build();
            return MethodSpec.methodBuilder("getInstances")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addCode(body)
                    .returns(TypeNames.parameterize(Iterable.class, TypeNames.REPOSITORY))
                    .build();
        }

        private static MethodSpec createInstanceFactory() {
            val cachingRepositoryType = ClassName.get(
                    Constants.CACHING_REPOSITORY_PACKAGE,
                    Constants.CACHING_REPOSITORY_NAME
            );
            val mergingRepositoryType = ClassName.get(
                    Constants.MERGING_REPOSITORY_PACKAGE,
                    Constants.MERGING_REPOSITORY_NAME
            );
            return MethodSpec.methodBuilder("getInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("return new $T(new $T(getInstances()))", cachingRepositoryType, mergingRepositoryType)
                    .returns(TypeNames.REPOSITORY)
                    .build();
        }

        private static MethodSpec createCheckerMethod() {
            return MethodSpec.methodBuilder("instantiated")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement(
                            "return $T.load($T.class).iterator().hasNext()",
                            ServiceLoader.class,
                            TypeNames.REPOSITORY
                    )
                    .returns(TypeName.BOOLEAN)
                    .build();
        }

        private static MethodSpec.Builder createGetter(String name, TypeName type) {
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Set.class), type));
        }
    }

    private static class MergingRepositoryClassGenerator {
        private static final String FIELD_NAME = "repositories";
        private static final TypeName FIELD_TYPE = ParameterizedTypeName.get(
                ClassName.get(List.class),
                TypeNames.REPOSITORY
        );
        private static final TypeName ITERABLE_TYPE = ParameterizedTypeName.get(
                ClassName.get(Iterable.class),
                TypeNames.REPOSITORY
        );

        public static CodeSource generate(List<SymbolSpec> symbols) {
            val field = createRepositoriesField();
            val builder = TypeSpec.classBuilder(Constants.MERGING_REPOSITORY_NAME)
                    .addSuperinterface(TypeNames.REPOSITORY)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(field)
                    .addMethod(createDefaultConstructor(field))
                    .addMethod(createIteratorConstructor(field));
            createGetters(symbols, field).forEach(builder::addMethod);
            return CodeSource.of(builder.build(), Constants.MERGING_REPOSITORY_PACKAGE);
        }

        private static MethodSpec createDefaultConstructor(FieldSpec field) {
            val argument = ParameterSpec.builder(FIELD_TYPE, FIELD_NAME).build();
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(argument)
                    .addStatement("this.$N = $N", field, argument)
                    .build();
        }

        private static MethodSpec createIteratorConstructor(FieldSpec field) {
            val argument = ParameterSpec.builder(ITERABLE_TYPE, FIELD_NAME).build();
            val body = CodeBlock.builder()
                    .add("this.$N = new $T<>();\n", field, ArrayList.class)
                    .beginControlFlow("for ($T repository : $N)", TypeNames.REPOSITORY, argument)
                    .add("this.$N.add(repository);\n", field)
                    .endControlFlow()
                    .build();
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(argument)
                    .addCode(body)
                    .build();
        }

        private static FieldSpec createRepositoriesField() {
            val type = ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    ClassName.get(Constants.REPOSITORY_PACKAGE, Constants.REPOSITORY_NAME)
            );
            return FieldSpec.builder(type, FIELD_NAME)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols, FieldSpec field) {
            return symbols.stream()
                    .flatMap(symbol -> {
                        return Stream.of(
                                createGreedyGetter(symbol, field),
                                createAnnotationGetter(symbol),
                                createStringGetter(symbol, field)
                        );
                    })
                    .collect(Collectors.toList());
        }

        private static MethodSpec createGreedyGetter(SymbolSpec<?> spec, FieldSpec field) {
            val name = "get" + Strings.capitalize(spec.getPluralName());
            val returnType = ParameterizedTypeName.get(ClassName.get(Set.class), spec.getGenerationType());
            val body = CodeBlock.of(
                    "return $N.stream()\n  .flatMap(repository -> repository.$L().stream())\n  .collect($T.toSet());\n",
                    field,
                    name,
                    Collectors.class
            );
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addCode(body)
                    .returns(returnType)
                    .build();
        }

        private static MethodSpec createAnnotationGetter(SymbolSpec<?> spec) {
            val name = "get" + Strings.capitalize(spec.getPluralName());
            val argument = ParameterSpec.builder(TypeNames.ANNOTATION_CLASS, ANNOTATION_ARGUMENT_NAME).build();
            val returnType = ParameterizedTypeName.get(TypeNames.SET, spec.getGenerationType());
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(argument)
                    .addStatement("return $N($N.getCanonicalName())", name, argument)
                    .returns(returnType)
                    .build();
        }

        private static MethodSpec createStringGetter(SymbolSpec<?> spec, FieldSpec field) {
            val name = "get" + Strings.capitalize(spec.getPluralName());
            val argument = ParameterSpec.builder(TypeNames.STRING, ANNOTATION_ARGUMENT_NAME).build();
            val returnType = ParameterizedTypeName.get(TypeNames.SET, spec.getGenerationType());
            val body = CodeBlock.of(
                    "return $N.stream()\n  .flatMap(repository -> repository.$L($N).stream())\n  .collect($T.toSet());\n",
                    field,
                    name,
                    argument,
                    Collectors.class
            );
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(argument)
                    .addCode(body)
                    .returns(returnType)
                    .build();
        }
    }

    private static class CachingRepositoryClassGenerator {
        public static CodeSource generate(List<SymbolSpec> symbols) {
            val delegate = createDelegateField();
            val builder = TypeSpec.classBuilder(Constants.CACHING_REPOSITORY_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(TypeNames.REPOSITORY)
                    .addField(delegate);
            createFields(symbols).forEach(builder::addField);
            builder.addMethod(createConstructor(delegate));
            createGetters(symbols, delegate).forEach(builder::addMethod);
            return CodeSource.of(builder.build(), Constants.CACHING_REPOSITORY_PACKAGE);
        }

        private static List<FieldSpec> createFields(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .flatMap(symbol -> Stream.of(createSetField(symbol), createMapField(symbol)))
                    .collect(Collectors.toList());
        }

        private static FieldSpec createDelegateField() {
            return FieldSpec.builder(TypeNames.REPOSITORY, "delegate")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
        }

        private static FieldSpec createMapField(SymbolSpec<?> symbol) {
            val type = TypeNames.mapOf(String.class, TypeNames.setOf(symbol.getGenerationType()));
            return FieldSpec.builder(type, symbol.getSingularName() + "Map")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T<>()", ConcurrentHashMap.class)
                    .build();
        }

        private static FieldSpec createSetField(SymbolSpec<?> symbol) {
            val setType = TypeNames.setOf(symbol.getGenerationType());
            val referenceType = TypeNames.parameterize(AtomicReference.class, setType);
            return FieldSpec.builder(referenceType, symbol.getSingularName() + "Set")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T<>()", AtomicReference.class)
                    .build();
        }

        private static MethodSpec createConstructor(FieldSpec field) {
            val argument = ParameterSpec.builder(TypeNames.REPOSITORY, field.name).build();
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(argument)
                    .addStatement("this.$N = $N", field, argument)
                    .build();
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols, FieldSpec delegate) {
            return symbols.stream()
                    .flatMap(symbol -> {
                        return Stream.of(
                                createGreedyGetter(symbol, delegate),
                                createAnnotationGetter(symbol),
                                createStringGetter(symbol, delegate)
                        );
                    })
                    .collect(Collectors.toList());
        }

        private static MethodSpec createGreedyGetter(SymbolSpec<?> symbol, FieldSpec delegate) {
            val name = "get" + Strings.capitalize(symbol.getPluralName());
            val setField = symbol.getSingularName() + "Set";
            val body = CodeBlock.builder()
                    .beginControlFlow("if ($L.get() == null)", setField)
                    .add("$L.set($N.$L());\n", setField, delegate, name)
                    .endControlFlow()
                    .add("return $L.get();\n", setField)
                    .build();
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addCode(body)
                    .returns(TypeNames.setOf(symbol.getGenerationType()))
                    .build();
        }

        private static MethodSpec createAnnotationGetter(SymbolSpec<?> symbol) {
            val argument = ParameterSpec.builder(TypeNames.ANNOTATION_CLASS, ANNOTATION_ARGUMENT_NAME).build();
            val name = "get" + Strings.capitalize(symbol.getPluralName());
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(argument)
                    .addStatement("return $N($N.getCanonicalName())", name, argument)
                    .returns(TypeNames.setOf(symbol.getGenerationType()))
                    .build();
        }

        private static MethodSpec createStringGetter(SymbolSpec<?> symbol, FieldSpec delegate) {
            val name = "get" + Strings.capitalize(symbol.getPluralName());
            val setField = symbol.getSingularName() + "Map";
            val argument = ParameterSpec.builder(TypeNames.STRING, ANNOTATION_ARGUMENT_NAME).build();
            val body = CodeBlock.builder()
                    .beginControlFlow("if (!$L.containsKey($N)) ", setField, argument)
                    .add("$L.put($N, $N.$L($N));\n", setField, argument, delegate, name, argument)
                    .endControlFlow()
                    .add("return $L.get($N);\n", setField, argument)
                    .build();
            return MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(argument)
                    .addCode(body)
                    .returns(TypeNames.setOf(symbol.getGenerationType()))
                    .build();
        }
    }
}
