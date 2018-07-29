package io.ayte.utility.discovery.api.v1.reference;

import io.ayte.utility.discovery.api.v1.TopElementReference;
import lombok.Data;

@Data
public class PackageReference implements TopElementReference<Package> {
    private final String name;

    public Package toSymbol() {
        return Package.getPackage(name);
    }
}
