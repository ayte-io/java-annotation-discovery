package io.ayte.utility.discovery.infrastructure.maven;

import com.squareup.javapoet.JavaFile;
import io.ayte.utility.discovery.infrastructure.Constants;
import io.ayte.utility.discovery.infrastructure.SymbolSpec;
import io.ayte.utility.discovery.infrastructure.generation.CodeGenerator;
import io.ayte.utility.discovery.infrastructure.generation.CodeSource;
import lombok.val;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractGenerationMojo extends AbstractMojo {
    protected abstract List<CodeGenerator<List<SymbolSpec>>> getGenerators();

    @Parameter(
            defaultValue = "${project.build.directory}/generated-sources/" + Constants.ROOT_PACKAGE,
            required = true,
            readonly = true
    )
    private String directory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        project.addCompileSourceRoot(directory);
        for (val generator : getGenerators()) {
            for (val source : generator.generate(Constants.PROCESSED_ELEMENTS)) {
                try {
                    write(source);
                } catch (IOException e) {
                    val name = source.getPackageName() + '.' + source.getSpec().name;
                    String message = "Failed to generate source file " + name + " emitted by" + generator;
                    throw new MojoExecutionException(message, e);
                }
            }
        }
    }

    private void write(CodeSource source) throws IOException  {
        val file = JavaFile.builder(source.getPackageName(), source.getSpec())
                .indent("    ")
                .skipJavaLangImports(true)
                .build();
        file.writeTo(Paths.get(directory));
    }
}
