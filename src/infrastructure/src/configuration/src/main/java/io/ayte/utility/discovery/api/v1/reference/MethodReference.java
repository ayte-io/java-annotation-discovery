package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.InnerElementReference;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Since annotation processing is rather limited, one can't get FQCN
 * for method arguments during compilation time. Because of that method
 * reference doesn't assume it has type information for arguments and
 * uses argument names to find method in case type information is
 * missing. The clients should be aware of that and treat nulls / be
 * ready for runtime exception during {@link #toSymbol} call. To
 * perform safe method lookup, clients can use {@link #toMatchingSymbols()}
 * method.
 * Type information still may be obtained during runtime scanning,
 * which may be implemented later, thus arguments have placeholders for
 * types.
 *
 * @param <T> Parent class / interface / annotation / enum
 */
@Data
@RequiredArgsConstructor
public class MethodReference<T> implements InnerElementReference<ClassElementReference<T>, Method> {
    private final ClassElementReference<T> parent;
    private final String name;
    private final List<Argument> arguments;

    public MethodReference(ClassElementReference<T> parent, String name, Argument... arguments) {
        this(parent, name, Arrays.asList(arguments));
    }

    @Override
    public Method toSymbol() throws ClassNotFoundException, NoSuchMethodException {
        val encloser = parent.toSymbol();
        boolean hasArgumentTypes = arguments.stream().map(Argument::getType).allMatch(Objects::nonNull);
        if (hasArgumentTypes) {
            return toDeterminateSymbol(encloser);
        }
        return toIndeterminateSymbol(encloser);
    }

    private Method toDeterminateSymbol(Class<?> parent) throws ClassNotFoundException, NoSuchMethodException {
        val types = new Class[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            types[i] = Class.forName(arguments.get(i).type);
        }
        return parent.getMethod(name, types);
    }

    private Method toIndeterminateSymbol(Class<?> parent) {
        val methods = toMatchingSymbols(parent);
        if (methods.size() > 1) {
            String message = "More than one method found for class " + parent + ", " +
                    "name " + name + " and arguments " + arguments;
            throw new RuntimeException(message);
        }
        return methods.isEmpty() ? null : methods.iterator().next();
    }

    public Set<Method> toMatchingSymbols() throws ClassNotFoundException {
        return toMatchingSymbols(parent.toSymbol());
    }

    private Set<Method> toMatchingSymbols(Class<?> parent) {
        val expectedNames = arguments.stream().map(Argument::getName).collect(Collectors.toList());
        return Arrays.stream(parent.getMethods())
                .filter(method -> method.getName().equals(name))
                .filter(method -> {
                    val names = Arrays.stream(method.getParameters())
                            .map(Parameter::getName)
                            .collect(Collectors.toList());
                    return names.equals(expectedNames);
                })
                .collect(Collectors.toSet());
    }

    @Data
    @RequiredArgsConstructor
    public static class Argument {
        private final String name;
        private final String type;

        public Argument(String name) {
            this(name, null);
        }
    }
}
