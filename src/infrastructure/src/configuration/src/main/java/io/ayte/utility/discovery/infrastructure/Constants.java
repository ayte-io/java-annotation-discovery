package io.ayte.utility.discovery.infrastructure;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Constants {
    private Constants() {}

    public static final String ROOT_PACKAGE = "io.ayte.utility.discovery";
    public static final String API_PACKAGE_NAME = ROOT_PACKAGE + ".api.v1";
    public static final String GENERATED_PACKAGE = ROOT_PACKAGE + ".generated";

    public static final String SYMBOL_COLLECTION_NAME = "SymbolCollection";
    public static final String SYMBOL_COLLECTION_PACKAGE = API_PACKAGE_NAME;

    public static final String EMPTY_SYMBOL_COLLECTION_NAME = "EmptySymbolCollection";
    public static final String EMPTY_SYMBOL_COLLECTION_PACKAGE = API_PACKAGE_NAME;

    public static final String DELEGATING_SYMBOL_COLLECTION_NAME = "DelegatingSymbolCollection";
    public static final String DELEGATING_SYMBOL_COLLECTION_PACKAGE = API_PACKAGE_NAME;

    public static final String ACCESSIBLE_SYMBOL_COLLECTION_NAME = "AccessibleSymbolCollection";
    public static final String ACCESSIBLE_SYMBOL_COLLECTION_PACKAGE = API_PACKAGE_NAME;

    public static final String REPOSITORY_NAME = "Repository";
    public static final String REPOSITORY_PACKAGE = API_PACKAGE_NAME;

    public static final String MERGING_REPOSITORY_NAME = "MergingRepository";
    public static final String MERGING_REPOSITORY_PACKAGE = API_PACKAGE_NAME;

    public static final String CACHING_REPOSITORY_NAME = "CachingRepository";
    public static final String CACHING_REPOSITORY_PACKAGE = API_PACKAGE_NAME;

    public static final String POPULATED_REPOSITORY_NAME = "PopulatedRepository";
    public static final String POPULATED_REPOSITORY_PACKAGE = GENERATED_PACKAGE;

    public static final String AUTO_DISCOVERY_NAME = "AutoDiscovery";
    public static final String AUTO_DISCOVERY_PACKAGE = API_PACKAGE_NAME;

    public static final Path GENERATED_RESOURCES_TARGET = Paths.get("META-INF/" + GENERATED_PACKAGE.replace('.', '/'));

    public static final List<SymbolSpec> PROCESSED_ELEMENTS = Collections.unmodifiableList(Symbols.LIST);
}
