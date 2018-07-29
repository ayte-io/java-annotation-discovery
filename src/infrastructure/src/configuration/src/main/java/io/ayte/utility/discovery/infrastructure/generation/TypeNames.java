package io.ayte.utility.discovery.infrastructure.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import io.ayte.utility.discovery.infrastructure.Constants;
import lombok.val;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class TypeNames {
    private TypeNames() {
        // static access only
    }

    public static final ClassName STRING = ClassName.get(String.class);
    public static final ClassName SET = ClassName.get(Set.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final TypeName ANNOTATION_CLASS = ParameterizedTypeName.get(
            ClassName.get(Class.class),
            WildcardTypeName.subtypeOf(ClassName.get(Annotation.class))
    );
    public static final ClassName REPOSITORY = ClassName.get(Constants.REPOSITORY_PACKAGE, Constants.REPOSITORY_NAME);

    public static TypeName wildcard(Class<?> type) {
        return ParameterizedTypeName.get(ClassName.get(type), WildcardTypeName.subtypeOf(Object.class));
    }

    public static ParameterizedTypeName setOf(Class<?> type) {
        return setOf(ClassName.get(type));
    }

    public static ParameterizedTypeName setOf(TypeName type) {
        return ParameterizedTypeName.get(SET, type);
    }

    public static ParameterizedTypeName mapOf(Class keys, Class values) {
        return mapOf(ClassName.get(keys), ClassName.get(values));
    }

    public static ParameterizedTypeName mapOf(Class keys, TypeName values) {
        return mapOf(ClassName.get(keys), values);
    }

    public static ParameterizedTypeName mapOf(TypeName keys, TypeName values) {
        return ParameterizedTypeName.get(MAP, keys, values);
    }

    public static ParameterizedTypeName parameterize(Class type, Class... parameters) {
        val converted = Arrays.stream(parameters).map(ClassName::get).toArray(TypeName[]::new);
        return parameterize(ClassName.get(type), converted);
    }

    public static ParameterizedTypeName parameterize(Class type, TypeName... parameters) {
        return parameterize(ClassName.get(type), parameters);
    }

    public static ParameterizedTypeName parameterize(ClassName type, TypeName... parameters) {
        return ParameterizedTypeName.get(type, parameters);
    }
}
