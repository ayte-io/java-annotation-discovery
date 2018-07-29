package io.ayte.utility.discovery.test.subject;

import io.ayte.utility.discovery.api.v1.AutoDiscovery;

import java.util.List;

@AutoDiscovery
public class Alpha<@AutoDiscovery T> {
    @AutoDiscovery
    private final T value = null; // NOSONAR

    @AutoDiscovery
    public interface Beta {}

    @AutoDiscovery
    public <Z> void method(@AutoDiscovery Z value, List<String> values) {} // NOSONAR

    @AutoDiscovery
    public enum Enumeration {
        @AutoDiscovery
        @Transitional
        CONSTANT
    }

    @AutoDiscovery(transitive = true)
    @interface Transitional {}
}
