package io.ayte.utility.discovery.infrastructure.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.misc.Strings;
import lombok.val;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1192"})
public class SymbolCollectionInfrastructureGenerator implements CodeGenerator<List<SymbolSpec>> {
    private static final ClassName INTERFACE_TYPE = ClassName.get(
            Constants.SYMBOL_COLLECTION_PACKAGE,
            Constants.SYMBOL_COLLECTION_NAME
    );

    @Override
    public Collection<CodeSource> generate(List<SymbolSpec> symbols) {
        return Arrays.asList(
                InterfaceGenerator.generate(symbols),
                EmptyCollectionGenerator.generate(symbols),
                DelegatingCollectionGenerator.generate(symbols),
                AccessibleCollectionGenerator.generate(symbols)
        );
    }

    private static MethodSpec.Builder startGetter(SymbolSpec symbol) {
        return MethodSpec.methodBuilder(Strings.getterOf(symbol.getPluralName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeNames.setOf(symbol.getGenerationType()));
    }

    private static class InterfaceGenerator {
        public static CodeSource generate(List<SymbolSpec> symbols) {
            val type = TypeSpec.interfaceBuilder(Constants.SYMBOL_COLLECTION_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethods(createGetters(symbols))
                    .build();
            return CodeSource.of(type, Constants.SYMBOL_COLLECTION_PACKAGE);
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(SymbolCollectionInfrastructureGenerator::startGetter)
                    .map(builder -> builder.addModifiers(Modifier.ABSTRACT))
                    .map(MethodSpec.Builder::build)
                    .collect(Collectors.toList());
        }
    }

    private static class EmptyCollectionGenerator {
        private static CodeSource generate(List<SymbolSpec> symbols) {
            val type = TypeSpec.classBuilder(Constants.EMPTY_SYMBOL_COLLECTION_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(INTERFACE_TYPE)
                    .addMethods(createGetters(symbols))
                    .build();
            return CodeSource.of(type, Constants.EMPTY_SYMBOL_COLLECTION_PACKAGE);
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols) {
            return symbols.stream().map(EmptyCollectionGenerator::createGetter).collect(Collectors.toList());
        }

        private static MethodSpec createGetter(SymbolSpec symbol) {
            return startGetter(symbol)
                    .addAnnotation(Override.class)
                    .addStatement("return $T.emptySet()", Collections.class)
                    .build();
        }
    }

    private static class DelegatingCollectionGenerator {
        public static final String DELEGATE_FIELD = "delegate";

        public static CodeSource generate(List<SymbolSpec> symbols) {
            val type = TypeSpec.classBuilder(Constants.DELEGATING_SYMBOL_COLLECTION_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(createDelegateField())
                    .addMethod(createConstructor())
                    .addMethods(createGetters(symbols))
                    .build();
            return CodeSource.of(type, Constants.DELEGATING_SYMBOL_COLLECTION_PACKAGE);
        }

        private static FieldSpec createDelegateField() {
            return FieldSpec.builder(INTERFACE_TYPE, DELEGATE_FIELD)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build();
        }

        private static MethodSpec createConstructor() {
            val argument = ParameterSpec.builder(INTERFACE_TYPE, DELEGATE_FIELD).build();
            return MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(argument)
                    .addStatement("this.$L = $N", DELEGATE_FIELD, argument)
                    .build();
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(symbol -> {
                        val method = Strings.getterOf(symbol.getPluralName());
                        return startGetter(symbol)
                                .addStatement("return $L.$L()", DELEGATE_FIELD, method)
                                .build();
                    })
                    .collect(Collectors.toList());
        }
    }

    private static class AccessibleCollectionGenerator {
        private static final ClassName TYPE = ClassName.get(
                Constants.ACCESSIBLE_SYMBOL_COLLECTION_PACKAGE,
                Constants.ACCESSIBLE_SYMBOL_COLLECTION_NAME
        );

        public static CodeSource generate(List<SymbolSpec> symbols) {
            val type = TypeSpec.classBuilder(Constants.ACCESSIBLE_SYMBOL_COLLECTION_NAME)
                    .addSuperinterface(INTERFACE_TYPE)
                    .addModifiers(Modifier.PUBLIC)
                    .addFields(createFields(symbols))
                    .addMethods(createGetters(symbols))
                    .addMethods(createSetters(symbols))
                    .addMethods(createAdders(symbols))
                    .addMethods(createCollectionAdders(symbols))
                    .addMethod(createMergeMethod(symbols))
                    .addMethod(createMapMergeMethod())
                    .build();
            return CodeSource.of(type, Constants.ACCESSIBLE_SYMBOL_COLLECTION_PACKAGE);
        }

        private static List<FieldSpec> createFields(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(AccessibleCollectionGenerator::createSymbolField)
                    .collect(Collectors.toList());
        }

        private static FieldSpec createSymbolField(SymbolSpec symbol) {
            return FieldSpec.builder(TypeNames.setOf(symbol.getGenerationType()), symbol.getPluralName())
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T<>()", HashSet.class)
                    .build();
        }

        private static List<MethodSpec> createGetters(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(AccessibleCollectionGenerator::createSymbolGetter)
                    .collect(Collectors.toList());
        }

        private static MethodSpec createSymbolGetter(SymbolSpec symbol) {
            return startGetter(symbol)
                    .addAnnotation(Override.class)
                    .addStatement("return new $T<>($L)", HashSet.class, symbol.getPluralName())
                    .build();
        }
        private static List<MethodSpec> createSetters(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(AccessibleCollectionGenerator::createSymbolSetter)
                    .collect(Collectors.toList());
        }

        private static MethodSpec createSymbolSetter(SymbolSpec symbol) {
            val type = TypeNames.setOf(symbol.getGenerationType());
            val argument = ParameterSpec.builder(type, symbol.getPluralName()).build();
            return MethodSpec.methodBuilder(Strings.getterOf(symbol.getPluralName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(argument)
                    .addStatement("this.$L.clear()", symbol.getPluralName())
                    .addStatement("this.$L.addAll($N)", symbol.getPluralName(), argument)
                    .addStatement("return this", HashSet.class, symbol.getPluralName())
                    .returns(TYPE)
                    .build();
        }

        private static List<MethodSpec> createAdders(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(AccessibleCollectionGenerator::createSymbolAdder)
                    .collect(Collectors.toList());
        }

        private static MethodSpec createSymbolAdder(SymbolSpec symbol) {
            val name = "another" + Strings.capitalize(symbol.getSingularName());
            val parameter = ParameterSpec.builder(symbol.getReferenceType(), name).build();
            return MethodSpec.methodBuilder("add" + Strings.capitalize(symbol.getSingularName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameter)
                    .addStatement("$L.add($N)", symbol.getPluralName(), parameter)
                    .addStatement("return this")
                    .returns(TYPE)
                    .build();
        }

        private static List<MethodSpec> createCollectionAdders(List<SymbolSpec> symbols) {
            return symbols.stream()
                    .map(AccessibleCollectionGenerator::createCollectionSymbolAdder)
                    .collect(Collectors.toList());
        }

        private static MethodSpec createCollectionSymbolAdder(SymbolSpec symbol) {
            val name = "more" + Strings.capitalize(symbol.getPluralName());
            val type = TypeNames.parameterize(Collection.class, symbol.getGenerationType());
            val parameter = ParameterSpec.builder(type, name).build();
            return MethodSpec.methodBuilder("add" + Strings.capitalize(symbol.getPluralName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameter)
                    .addStatement("$L.addAll($N)", symbol.getPluralName(), parameter)
                    .addStatement("return this")
                    .returns(TYPE)
                    .build();
        }

        private static MethodSpec createMergeMethod(List<SymbolSpec> symbols) {
            val emptyType = ClassName.get(
                    Constants.EMPTY_SYMBOL_COLLECTION_PACKAGE,
                    Constants.EMPTY_SYMBOL_COLLECTION_NAME
            );
            val left = ParameterSpec.builder(INTERFACE_TYPE, "left").build();
            val right = ParameterSpec.builder(INTERFACE_TYPE, "right").build();
            val body = CodeBlock.builder()
                    .beginControlFlow("if ($N == null)", left)
                    .addStatement("$N = new $T()", left, emptyType)
                    .nextControlFlow("else if ($N == null)", right)
                    .addStatement("$N = new $T()", left, emptyType)
                    .endControlFlow()
                    .addStatement("$T accumulator = new $T()", TYPE, TYPE);
            symbols.forEach(symbol -> {
                val adder = "add" + Strings.capitalize(symbol.getSingularName());
                val getter = Strings.getterOf(symbol.getPluralName());
                Stream.of(left, right).forEach(argument -> {
                    body.addStatement("$N.$L().forEach(accumulator::$L)", argument, getter, adder);
                });
            });
            body.addStatement("return accumulator");
            return MethodSpec.methodBuilder("merge")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(left)
                    .addParameter(right)
                    .addCode(body.build())
                    .returns(TYPE)
                    .build();
        }

        private static MethodSpec createMapMergeMethod() {
            val operationType = TypeNames.mapOf(String.class, TYPE);
            val argumentType = TypeNames.mapOf(String.class, WildcardTypeName.subtypeOf(INTERFACE_TYPE));
            val left = ParameterSpec.builder(argumentType, "left").build();
            val right = ParameterSpec.builder(argumentType, "right").build();
            val body = CodeBlock.builder()
                    .addStatement("$T accumulator = new $T<>()", operationType, HashMap.class)
                    .add("$T.of($N, $N)\n", Stream.class, left, right)
                    .indent()
                    .add(".map($T::keySet)\n", Map.class)
                    .add(".flatMap($T::stream)\n", Set.class)
                    .add(".forEach(key -> accumulator.put(key, merge($N.get(key), $N.get(key))));\n", left, right)
                    .unindent()
                    .addStatement("return accumulator")
                    .build();
            return MethodSpec.methodBuilder("merge")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(left)
                    .addParameter(right)
                    .addCode(body)
                    .returns(operationType)
                    .build();
        }
    }
}
