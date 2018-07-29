package io.ayte.utility.discovery.infrastructure.maven;

import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.AutoDiscoveryAnnotationGenerator;
import io.ayte.utility.discovery.infrastructure.generation.CodeGenerator;
import io.ayte.utility.discovery.infrastructure.generation.RepositoryInfrastructureGenerator;
import io.ayte.utility.discovery.infrastructure.generation.SymbolCollectionInfrastructureGenerator;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Arrays;
import java.util.List;

@Mojo(name = "generate")
public class ApiGeneratorMojo extends AbstractGenerationMojo {
    @Override
    protected List<CodeGenerator<List<SymbolSpec>>> getGenerators() {
        return Arrays.asList(
                new SymbolCollectionInfrastructureGenerator(),
                new RepositoryInfrastructureGenerator(),
                new AutoDiscoveryAnnotationGenerator()
        );
    }
}
