package io.ayte.utility.discovery.infrastructure.misc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class Pair<X, Y> {
    private final X left;
    private final Y right;

    public static <X, Y> Pair<X, Y> of(X left, Y right) {
        return new Pair<>(left, right);
    }
}
